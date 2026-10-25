# MiniLedger

> **Your lending, clearly tracked.**  
> A simple, clean way to record personal loans, track repayments, and keep things sorted between friends or family without any awkwardness.

![MiniLedger Screens](../image.png)

*Design showcase. The React Native app and native Android preview have separate implementations; their screens and interactions may differ.*

**For reviewers:** the assignment implementation is **`mobile/` + `backend/`**. The downloadable **MiniLedger Preview APK** is the separate Kotlin/Compose design prototype in `app/`, with local Room storage. It does not demonstrate the React Native/PostgreSQL integration. See [APK instructions](releases/README.md).
---

## Why I Built This

Lending small amounts of money to friends or colleagues happens all the time, but keeping track of who owes what—or who has already paid you back—usually ends up messy in chat notes or scraps of paper.

I built **MiniLedger** to keep this process dead simple, private, and painless. It gives you a clear dashboard showing total money lent, money repaid, and anything pending or overdue, wrapped in a warm, clean UI.

---

## What It Does

- **Passwordless Phone Login:** Log in using just your phone number and OTP (printed straight to the backend console for local testing—no third-party SMS setup needed).
- **At-a-Glance Dashboard:** Shows your Total Lent, Repaid, and Overdue amounts right up top.
- **Quick Loan Entry:** Add a borrower's name, loan amount (with quick-select chip suggestions), due date, and a note.
- **Outstanding Loans & History:** Loans groups unpaid entries by friend and lets you mark them repaid. History includes All, Pending, Repaid, and Overdue filters. The native preview also has a loan-details bottom sheet.
- **Per-User Isolation:** Authenticated API requests only access the signed-in user's loans in PostgreSQL.
- **Profile & Settings:** Edit your profile, export loans as CSV, and browse dedicated privacy/help pages. Profile name, optional email, and photo are saved locally on the device; they are not synchronized through PostgreSQL. Scheduled push reminders and selectable themes are not implemented.

---

## Tech Stack

- **Mobile:** React Native, Expo (SDK 58), TypeScript
- **Backend:** Node.js, Express, TypeScript
- **Database:** PostgreSQL
- **Design Preview (Native):** Jetpack Compose in `app/` (for UI reference)

---

## Getting Started

### Prerequisites
- Node.js **22.13 or newer within the 22.x line**, or **24.3+ within 24.x** (required by the installed React Native dependencies)
- PostgreSQL 16+
- An Expo Go version compatible with **SDK 58**, on a supported phone or emulator. Android and web are the tested targets; iOS has not been verified.
- Android SDK Platform Tools (`adb`) if testing over Android USB

---

### 1. Backend Setup

1. Open your terminal and jump into the `backend` folder:
   ```bash
   cd backend
   ```
2. Install dependencies:
   ```bash
   npm ci
   ```
3. Create your `.env` file from the sample:
   ```bash
   cp .env.example .env
   ```
   In PowerShell, you can also use `Copy-Item .env.example .env`.
   Open `.env` and set your database connection string and a secret key:
   ```env
   PORT=5001
   DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@127.0.0.1:5432/miniledger
   JWT_SECRET=PASTE_YOUR_GENERATED_SECRET_HERE
   ```
   Generate a random secret, then paste its output into `JWT_SECRET`:
   ```bash
   node -e "console.log(require('node:crypto').randomBytes(32).toString('hex'))"
   ```
   The backend requires a secret of **at least 32 characters**. Replace `YOUR_PASSWORD` with your PostgreSQL password, URL-encoding special characters if necessary. Do not commit `.env`.

   Create the database once using psql or pgAdmin:
   ```sql
   CREATE DATABASE miniledger;
   ```
   Tables are created automatically on backend startup. The connection user needs permission to create and alter those tables. Port `5432` is the usual PostgreSQL default; the author's separate local development cluster uses **55432**, so keep that port when using the existing local setup.

4. Build and start the backend:
   ```bash
   npm run build
   npm start
   ```
   The backend will boot up on `http://localhost:5001`. Keep this terminal open! When you log in from the mobile app, the **OTP will print right here** in the console.

---

### 2. Mobile App Setup

1. In a new terminal window, head to `mobile`:
   ```bash
   cd mobile
   ```
   Run this from the project root (or `cd ../mobile` if your terminal is still in `backend`).
2. Install dependencies:
   ```bash
   npm ci
   ```
3. Set your backend URL in `.env`:
   ```env
   EXPO_PUBLIC_API_URL=http://<YOUR_COMPUTER_LAN_IP>:5001
   ```
   For Wi-Fi testing, use your computer's actual LAN address, such as `http://192.168.1.20:5001`. Keep the phone and computer on the same network and allow the development server through the firewall on that private network. `localhost` on a phone refers to the phone itself.

4. **Choose a connection method.** For Wi-Fi, start Expo and scan its QR code:
   ```bash
   npx expo start --lan
   ```

   **For Android USB debugging**, instead set `EXPO_PUBLIC_API_URL=http://127.0.0.1:5001` in `mobile/.env`, connect and authorize the phone, then run:
   ```bash
   adb reverse tcp:5001 tcp:5001
   adb reverse tcp:8081 tcp:8081
   ```

   On Windows PowerShell, start Metro with an explicit USB-compatible host:
   ```powershell
   $env:REACT_NATIVE_PACKAGER_HOSTNAME = "127.0.0.1"
   npx expo start --lan --port 8081 --max-workers 2
   ```
   On macOS/Linux:
   ```bash
   REACT_NATIVE_PACKAGER_HOSTNAME=127.0.0.1 npx expo start --lan --port 8081 --max-workers 2
   ```
   Open `exp://127.0.0.1:8081` in Expo Go, or launch it from another terminal:
   ```bash
   adb shell am start -a android.intent.action.VIEW -d exp://127.0.0.1:8081 -p host.exp.exponent
   ```

5. Keep PostgreSQL, the backend, and Metro running. Reapply `adb reverse` after reconnecting USB. On this Windows setup, avoid Expo's `--localhost` flag: it previously bound only to IPv6 and caused the phone's remote-update download error. Restart Metro after changing `.env`.

For an Android emulator without reverse forwarding, use `http://10.0.2.2:5001` as the API URL. For a browser running on the backend computer, use `http://127.0.0.1:5001` and press `w` in Expo.

---

## How to Test the Flow

1. Open the app on your phone or browser.
2. Enter a full international phone number, including the country code, such as `+919876543210`.
3. Check your backend terminal window for the 6-digit OTP log:
   ```text
   [MiniLedger mock OTP] +919876543210: 123456 (expires in 5 minutes)
   ```
   This is an example format; use the newly generated code in your own log. No SMS is sent and the code is not returned in the API response.
4. Enter the OTP within five minutes. Each code is single-use, has a five-attempt limit, and can be resent after 30 seconds.
5. Open **Add loan**, enter a name, amount and due date, then save. Use a date before today to demonstrate overdue highlighting.
6. Mark the loan repaid in **Loans**. Total Repaid increases and Overdue decreases if applicable; Total Lent stays unchanged. The entry remains in History.

For live database queries and a recording sequence, see [VIDEO-DEMO.md](docs/VIDEO-DEMO.md). The viewer in `backend/scripts/show-data.cjs` reads actual PostgreSQL rows. Only OTP delivery is mocked; loan records are persisted through the API.

---

## Running Tests

- **Backend tests:**
  ```bash
  cd backend
  npm test
  ```
  Requires the backend `.env` and a running PostgreSQL database. Runs HTTP acceptance tests covering authentication, OTP response privacy, resend cooldown, wrong/reused codes, input validation, loan persistence, user isolation, repayment idempotency, and summary totals. Tests create disposable accounts and remove their own records afterwards. Expiry is enforced by the backend but is not explicitly exercised by this test suite.

- **Mobile typecheck:**
  ```bash
  cd mobile
  npm run typecheck
  npm run lint
  npx expo export --platform android --platform web --max-workers 2
  ```

---

## Project Structure

```text
miniledger/
├── backend/          # Node.js + Express + TypeScript API with PostgreSQL
├── mobile/           # React Native + Expo app
├── app/              # Native Android Jetpack Compose reference prototype
├── docs/             # Additional guides and demo notes
├── scripts/          # Local development restart helper
├── releases/         # APK review instructions and checksum (APK attached separately)
└── image.png         # UI mockup showcase
```

## Downloadable Preview

The prepared file is `releases/MiniLedger-Preview-v1.0.apk`. It is a signed debug build of the **native Android preview**, with local loan storage and mock login. APK binaries are ignored by Git; attach it to a GitHub Release so reviewers can download it. See [releases/README.md](releases/README.md) for installation and mock-login instructions.

The React Native app currently runs through Expo Go. A standalone React Native APK and publicly hosted backend are not included; remote reviewers must run the setup above. Never publish `.env`, signing keys, database files, or server logs.
