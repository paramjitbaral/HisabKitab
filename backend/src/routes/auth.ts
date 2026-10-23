import { Router } from "express";
import jwt from "jsonwebtoken";
import { randomInt } from "crypto";
import { query } from "../db";
import { jwtSecret } from "../config";
const router = Router();
const normalize = (value: unknown) =>
  typeof value === "string" ? value.replace(/[\s()-]/g, "") : "";
const phoneValid = (value: string) => /^\+[1-9]\d{7,14}$/.test(value);
const busy = new Set<string>();
router.post("/send-otp", async (req, res) => {
  const phone = normalize(req.body.phoneNumber);
  if (!phoneValid(phone))
    return res
      .status(400)
      .json({
        error: "Use an international phone number, for example +919876543210.",
      });
  if (busy.has(phone))
    return res
      .status(429)
      .json({ error: "Please wait before requesting another code." });
  busy.add(phone);
  try {
    const recent = await query(
      `SELECT id FROM otps WHERE phone_number=$1 AND created_at > NOW() - INTERVAL '30 seconds' LIMIT 1`,
      [phone],
    );
    if (recent.rows.length)
      return res
        .status(429)
        .json({
          error: "Please wait 30 seconds before requesting another code.",
        });
    const code = randomInt(100000, 1000000).toString();
    await query(
      "UPDATE otps SET is_used=TRUE WHERE phone_number=$1 AND is_used=FALSE",
      [phone],
    );
    await query(
      `INSERT INTO otps(phone_number,otp_code,expires_at) VALUES ($1,$2,NOW()+INTERVAL '5 minutes')`,
      [phone, code],
    );
    console.log(
      `[MiniLedger mock OTP] ${phone}: ${code} (expires in 5 minutes)`,
    );
    return res.json({
      success: true,
      mode: "mock",
      message: "Check the backend server log for your 6-digit code.",
    });
  } catch {
    return res
      .status(500)
      .json({ error: "Unable to request a code. Please try again." });
  } finally {
    busy.delete(phone);
  }
});
router.post("/verify-otp", async (req, res) => {
  const phone = normalize(req.body.phoneNumber);
  const code = typeof req.body.otp === "string" ? req.body.otp.trim() : "";
  if (!phoneValid(phone) || !/^\d{6}$/.test(code))
    return res
      .status(400)
      .json({ error: "A valid phone number and six-digit code are required." });
  try {
    // Increment every attempt atomically and lock the same row before consuming it.
    const attempt = await query(
      `UPDATE otps SET attempts=attempts+1 WHERE id=(SELECT id FROM otps WHERE phone_number=$1 AND is_used=FALSE AND expires_at>NOW() ORDER BY id DESC LIMIT 1) AND attempts<5 AND is_used=FALSE RETURNING id,otp_code`,
      [phone],
    );
    const record = attempt.rows[0];
    if (!record || record.otp_code !== code)
      return res
        .status(400)
        .json({
          error:
            "Invalid, expired or locked code. Request a new code after 30 seconds.",
        });
    const used = await query(
      "UPDATE otps SET is_used=TRUE WHERE id=$1 AND is_used=FALSE RETURNING id",
      [record.id],
    );
    if (!used.rows.length)
      return res
        .status(400)
        .json({ error: "This code has already been used." });
    const result = await query(
      `INSERT INTO users(phone_number) VALUES($1) ON CONFLICT(phone_number) DO UPDATE SET phone_number=EXCLUDED.phone_number RETURNING id,phone_number`,
      [phone],
    );
    const user = result.rows[0];
    const token = jwt.sign(
      { userId: user.id, phoneNumber: user.phone_number },
      jwtSecret!,
      { expiresIn: "7d" },
    );
    return res.json({
      success: true,
      token,
      user: { id: user.id, phoneNumber: user.phone_number },
    });
  } catch {
    return res
      .status(500)
      .json({ error: "Unable to verify code. Please try again." });
  }
});
export default router;
