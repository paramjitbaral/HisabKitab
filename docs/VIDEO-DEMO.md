# MiniLedger recording guide

## Which app and code to show

Project folder: `C:\Users\swara\Downloads\miniledger`.

Use **MiniLedger inside Expo Go** for the assignment recording. This is the React Native app connected to the Node.js/TypeScript API and PostgreSQL. The separately installed **MiniLedger Preview** is the older Kotlin/Room design preview; its local records do not demonstrate this PostgreSQL backend and are not migrated automatically.

| Requirement | Code | What to demonstrate |
| --- | --- | --- |
| React Native login and OTP | mobile/src/app/login.tsx | Request and verify a mock code |
| Node.js + TypeScript API | backend/src/index.ts | Running Express server on port 5001 |
| Mock OTP | backend/src/routes/auth.ts | Server generates/logs code; database records expiry/use |
| PostgreSQL connection | backend/src/db.ts | pg.Pool connection to actual PostgreSQL |
| Database tables | backend/src/schema.sql | users, otps, loans |
| Add/list/repay APIs | backend/src/routes/loans.ts | Create record, then update repayment |
| Summary API | backend/src/routes/summary.ts | Totals computed from stored records |
| Summary screen | mobile/src/app/(tabs)/home.tsx | Lent, repaid and overdue amounts |
| Outstanding loans | mobile/src/app/(tabs)/loans.tsx | Open balances grouped by friend |
| History | mobile/src/app/(tabs)/history.tsx | Open and repaid entries with filters |
| Add form | mobile/src/app/(tabs)/add.tsx | Name, amount, date and optional note |

Open the project folder in VS Code with File > Open Folder. You can also open it in File Explorer by pasting the project path in the address bar.

## Where the OTP goes

No SMS is sent. Pressing Send OTP calls `POST /auth/send-otp`. The backend generates a six-digit code, saves it in PostgreSQL's **otps** table, and prints it in the server log. It is not returned in the API response. It expires after five minutes; successful verification sets `is_used=true`. Resending has a 30-second cooldown.

In PowerShell, show the current running server's log live:

```powershell
Set-Location C:\Users\swara\Downloads\miniledger
Get-Content .\output\backend.log -Tail 10 -Wait
```

Leave this terminal visible and press Send OTP in Expo Go. Use the newest code for that phone number. Ctrl+C stops log watching, not the backend. If you instead start the backend with `npm start` in a visible terminal, its OTPs appear directly in that terminal.

## Where the database is

The running development server uses real **PostgreSQL 16**, database/user `miniledger`, host `127.0.0.1`, port **55432**. Its disk cluster is `backend/.postgres`. These are PostgreSQL-managed files, not readable spreadsheets; query the database to see rows. Credentials are in ignored `backend/.env`. Do not display that file or JWT tokens in your video.

The system PostgreSQL service on port 5432 is a different instance. Use 55432 for this project.

### Easiest live view for the recording

Open a second PowerShell window. Replace `+91YOURNUMBER` with the SAME full phone number used to sign in:

```powershell
Set-Location C:\Users\swara\Downloads\miniledger
& .\output\runtime\node_modules\node\bin\node.exe .\backend\scripts\show-data.cjs '+91YOURNUMBER' --watch
```

This read-only viewer refreshes every two seconds and shows the server/database identity, OTP rows, account, loans and totals. It reads credentials privately from .env. It does not create fake records or modify anything. Ctrl+C exits. Omit `--watch` for a single snapshot.

### Open an actual PostgreSQL SQL terminal

```powershell
& .\output\runtime\node_modules\node\bin\node.exe .\backend\scripts\open-db.cjs
```

This opens psql in read-only mode without printing the password. Commands to demonstrate:

```sql
\conninfo
\dt
\d otps
\d loans
SELECT id, phone_number, otp_code, expires_at, is_used FROM otps ORDER BY id DESC LIMIT 5;
SELECT id, phone_number, created_at FROM users;
SELECT id, user_id, friend_name, amount, due_date, note, is_repaid, repaid_at FROM loans ORDER BY id DESC;
\q
```

The SQL examples show all accounts. For a public video, prefer the phone-filtered viewer above and a dedicated demo account.

## Suggested 4–5 minute video

1. Show `mobile/`, `backend/src/` and `schema.sql`. Explain React Native → HTTP API → PostgreSQL.
2. Show the live server terminal and database viewer side by side with Expo Go. Sign out first if needed.
3. Enter a demo phone number and Send OTP. Point out the new log line and `otps` row. No real SMS is sent; this is the requested mock delivery.
4. Enter the code. Show `is_used=true` and the account in `users`.
5. Add “Demo Friend”, ₹1,250, a date BEFORE today and note “Video demo”. Show the new `loans` row and its `is_repaid=false` status.
6. Show Home: total lent increases by ₹1,250 and overdue increases by ₹1,250. Loans highlights the overdue entry.
7. Mark the entry repaid. Show `is_repaid=true` and `repaid_at` populated. Total lent stays the same; repaid increases and overdue decreases by ₹1,250. History still contains the record.
8. Refresh or reopen the app to demonstrate persistence. Show the matching database row again.
9. Optionally show `npm test` in backend, which exercises HTTP OTP/loan flows against PostgreSQL using disposable accounts.

Use a fresh account for exactly ₹1,250 totals; existing accounts include their earlier records. The app uses real API/database records, not prefilled mock loans. Only OTP delivery is mocked.

## Keep the phone connected

After restarting Windows, run this once from the project folder to restart the existing local database, backend and Metro (without starting duplicates):

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-demo.ps1
```

This command applies the execution-policy option only to this script process; it does not change your system policy. Existing OTP logs are archived before a backend restart. Wait for Metro to start, then open Expo Go.

Metro (8081), backend (5001) and PostgreSQL (55432) must stay running during an Expo USB demo. Reapply forwarding after reconnecting:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s dccba126 reverse tcp:5001 tcp:5001
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s dccba126 reverse tcp:8081 tcp:8081
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s dccba126 shell am start -a android.intent.action.VIEW -d 'exp://127.0.0.1:8081' -p host.exp.exponent
```

For server restart/setup instructions see the root README. Profile name, optional email and selected photo are device-local preferences; the PostgreSQL assignment tables store authentication and loans. Your Data exports loaded loans as CSV. Push notifications and selectable themes are not implemented, so no inactive controls are shown for them.
