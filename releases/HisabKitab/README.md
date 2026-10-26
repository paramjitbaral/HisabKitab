# HisabKitab 1.0.0

Developer: Paramjit Baral  
Support: aapnastoreofficial@gmail.com

- **HisabKitab-1.0.0.apk** — signed standalone app for your website, Android 7.0+.
- **HisabKitab-1.0.0.aab** — signed Google Play upload bundle.
- **SHA256SUMS.txt** — integrity hashes.
- **index.html** — download page; upload with the APK, privacy page, checksum and store-assets folder.
- **privacy.html / PRIVACY.md** — public privacy policy, also explained inside the app.
- **store-assets/** — 512px icon, 1024×500 feature graphic and sample-data screenshots.

The app is fully offline: no login, OTP, server, ads or analytics. It records lending and repayments; it does not lend or transfer money. Amounts use INR.

Loan data stays on the device. Save a JSON backup from Profile → Your Data before changing phones, uninstalling or clearing storage. Restore merges records and skips exact duplicates. CSV exports are for spreadsheets and cannot be restored. Backups are not encrypted.

The production package is com.paramjitbaral.hisabkitab and is separate from the earlier preview. To move records, save a JSON backup in the updated preview and restore it in HisabKitab.

Before Google Play submission, host privacy.html on a public HTTPS URL, complete Play Console verification/declarations/testing and configure Play App Signing to use the existing signing key if website installations should update seamlessly. See RELEASE-GUIDE.md in this package.

Do not include signing keys, passwords, databases, logs or source .env files in your website upload. Signing material is stored outside this release folder.
