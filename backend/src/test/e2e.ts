import "dotenv/config";
import assert from "node:assert/strict";
import app from "../index";
import { initDb, query, closeDb } from "../db";
async function main() {
  await initDb();
  const server = app.listen(0, "127.0.0.1");
  await new Promise<void>((r) => server.once("listening", r));
  const address = server.address() as { port: number };
  const url = `http://127.0.0.1:${address.port}`;
  const phones = [
    "+1999" + String(Date.now()).slice(-7),
    "+1888" + String(Date.now()).slice(-7),
  ];
  const codes = new Map<string, string>();
  const original = console.log;
  console.log = (...args) => {
    const line = args.join(" ");
    const match = line.match(/\[MiniLedger mock OTP\] (\+\d+): (\d{6})/);
    if (match) codes.set(match[1], match[2]);
    else original(...args);
  };
  async function request(
    path: string,
    method = "GET",
    body?: unknown,
    token?: string,
  ) {
    const response = await fetch(url + path, {
      method,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    return { status: response.status, data: (await response.json()) as any };
  }
  try {
    assert.equal((await request("/loans")).status, 401);
    assert.equal(
      (await request("/auth/send-otp", "POST", { phoneNumber: "bad" })).status,
      400,
    );
    const tokens: string[] = [];
    for (const phoneNumber of phones) {
      const sent = await request("/auth/send-otp", "POST", { phoneNumber });
      assert.equal(sent.status, 200);
      assert.equal(sent.data.mockOtp, undefined);
      assert.equal(
        (await request("/auth/send-otp", "POST", { phoneNumber })).status,
        429,
      );
      const otp = codes.get(phoneNumber);
      assert.ok(otp);
      assert.equal(
        (
          await request("/auth/verify-otp", "POST", {
            phoneNumber,
            otp: "000000",
          })
        ).status,
        400,
      );
      const verified = await request("/auth/verify-otp", "POST", {
        phoneNumber,
        otp,
      });
      assert.equal(verified.status, 200);
      tokens.push(verified.data.token);
      assert.equal(
        (await request("/auth/verify-otp", "POST", { phoneNumber, otp }))
          .status,
        400,
      );
    }
    const token = tokens[0];
    assert.deepEqual(
      (await request("/summary", "GET", undefined, token)).data,
      { totalLent: 0, totalRepaid: 0, overdueAmount: 0 },
    );
    const input = {
      friendName: "Acceptance friend",
      amount: 250.5,
      dueDate: "2020-01-01",
      note: "API test",
    };
    assert.equal(
      (await request("/loans", "POST", { ...input, amount: "25oops" }, token))
        .status,
      400,
    );
    assert.equal(
      (
        await request(
          "/loans",
          "POST",
          { ...input, dueDate: "2026-02-30" },
          token,
        )
      ).status,
      400,
    );
    const created = await request("/loans", "POST", input, token);
    assert.equal(created.status, 201);
    assert.equal(created.data.note, "API test");
    assert.equal(
      (await request("/loans", "GET", undefined, token)).data.length,
      1,
    );
    assert.equal(
      (await request("/loans", "GET", undefined, tokens[1])).data.length,
      0,
    );
    assert.equal(
      (await request(`/loans/${created.data.id}/repay`, "PATCH", {}, tokens[1]))
        .status,
      404,
    );
    assert.deepEqual(
      (await request("/summary", "GET", undefined, token)).data,
      { totalLent: 250.5, totalRepaid: 0, overdueAmount: 250.5 },
    );
    const repaid = await request(
      `/loans/${created.data.id}/repay`,
      "PATCH",
      {},
      token,
    );
    assert.equal(repaid.status, 200);
    assert.equal(
      (await request(`/loans/${created.data.id}/repay`, "PATCH", {}, token))
        .data.repaidAt,
      repaid.data.repaidAt,
    );
    assert.deepEqual(
      (await request("/summary", "GET", undefined, token)).data,
      { totalLent: 250.5, totalRepaid: 250.5, overdueAmount: 0 },
    );
    original(
      "PASS: HTTP authentication, OTP privacy/throttle/replay, validation, loan persistence, user isolation, repayment and summary.",
    );
  } finally {
    console.log = original;
    await query("DELETE FROM users WHERE phone_number=ANY($1::text[])", [
      phones,
    ]);
    await query("DELETE FROM otps WHERE phone_number=ANY($1::text[])", [
      phones,
    ]);
    await new Promise<void>((r, j) => server.close((e) => (e ? j(e) : r())));
    await closeDb();
  }
}
main().catch((e) => {
  console.error(e);
  process.exitCode = 1;
});
