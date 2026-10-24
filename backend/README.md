# MiniLedger API

Node.js, TypeScript, Express and PostgreSQL. See the root README for setup and USB instructions.

Run npm ci, configure .env, then npm run build and npm start. An invalid DATABASE_URL fails startup. Embedded PGlite is only selected explicitly with DB_MODE=pglite and no DATABASE_URL.

- POST /auth/send-otp: phoneNumber in international format. Code appears only in the server log. Resend cooldown: 30 seconds.
- POST /auth/verify-otp: phoneNumber and six-digit otp. Returns token and user. Five-minute expiry, five-attempt limit, single use; no hardcoded fallback.
- GET /loans: authenticated user's entries.
- POST /loans: friendName, amount, dueDate (YYYY-MM-DD), optional note. Positive amount, at most two decimal places.
- PATCH /loans/:id/repay: idempotent repayment, owner only.
- GET /summary: totalLent, totalRepaid, overdueAmount. Overdue means unpaid and due before the PostgreSQL session's current date.
- GET /health: server health.

Protected routes require Authorization: Bearer followed by the token. npm test runs actual HTTP acceptance tests against configured PostgreSQL. Mock OTP is development-only; no SMS provider is configured.
