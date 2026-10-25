# HisabKitab release handoff

## Files
- `releases/HisabKitab/HisabKitab-1.0.0.apk`: signed standalone Android release for your website.
- `releases/HisabKitab/HisabKitab-1.0.0.aab`: bundle for Google Play.
- `releases/HisabKitab/SHA256SUMS.txt`: checksums.
- `releases/HisabKitab/store-assets/`: icon, feature graphic and screenshots using fictional sample records.

The release is the offline Kotlin/Compose app. The separate React Native/PostgreSQL assignment is not shipped in this APK. Release package ID is `com.paramjitbaral.hisabkitab`; visible name is HisabKitab. New debug builds use `com.paramjitbaral.hisabkitab.preview`. Previously installed previews remain on the phone with their existing data. Once published, keep that package ID. Release versionCode is 2, versionName 1.0.0, minSdk 24, targetSdk 36.

## Signing and future updates
The build script stores the PKCS12 key and Windows DPAPI-protected password in `%USERPROFILE%/.hisabkitab-signing/`, outside Git. Alias: `upload`. Back up the keystore and securely save its password in a password manager. The DPAPI password file alone cannot be decrypted on another Windows account/machine. Do not distribute this folder with the APK or source.

For a later Google Play launch, choose the Play App Signing option to use your existing app signing key if you want Play-installed updates to replace website-installed copies. Letting Google generate a different app-signing key would prevent those installations from updating seamlessly. Follow Play Console's secure key-upload workflow; never send the key to reviewers. Increment versionCode for every published update. APK and AAB must use the same package identity.

A production installation is separate from the `.preview` app used during development. Use Profile → Your Data → Save backup in the updated preview, then Restore backup in HisabKitab to transfer your loans. Do not uninstall the preview before backing up.

## Public listing text
Name: HisabKitab
Short description: Track personal loans, due dates and repayments offline. No sign-up needed.

Full description:
HisabKitab keeps a clear record of money you lend to friends and family. Add a name, amount, due date and note, then record repayment when the full amount is returned.
See total lent, repaid and overdue amounts. Group outstanding balances by person, or browse and filter your complete loan history. Export a spreadsheet-friendly CSV or save a JSON backup you can restore on another phone.
Works offline, with no account, phone login, ads or analytics. Your records stay on your device. You control backups and deletion from Settings.
HisabKitab is a personal record keeper. It does not provide loans, move money, calculate interest, offer financial advice or send repayment notifications. Currency: INR. Uninstalling removes local records, so save a backup first.

## Website publication
Upload only the signed APK, checksum, listing images and finalized public privacy page. Publish the privacy page on a public HTTPS URL. Developer: Paramjit Baral. Support: aapnastoreofficial@gmail.com. The privacy page is included as `privacy.html`; host it on your public HTTPS website and use that URL in Play Console. Do not upload the signing key or DPAPI file. The download should clearly say Android 7.0+ and offline storage.

## Google Play tasks that remain in your account
- Complete developer verification and any required account testing/access steps.
- Upload the AAB and choose the signing-key migration above.
- Supply a support email and public privacy-policy URL; link the finalized policy in the listing.
- Complete Data safety according to this exact binary: no app-operated off-device data collection/sharing; user-selected file exports/storage providers are user initiated. Reassess if SDKs or features change.
- Complete the financial features declaration truthfully for a personal record-keeping app. It does not issue or broker loans or process payments. Use the categories and guidance currently shown in Play Console.
- Complete content rating, target audience, ads (none), app access (no login), and other applicable declarations.
- Run internal/closed testing and the Play pre-launch report. Fix any device-specific issues flagged before production. For eligible new personal accounts, Google currently requires 12 continuously opted-in testers for 14 days before applying for production access.
- Approval is determined by Google; a signed artifact alone does not constitute approval.

Official sources checked October 26, 2026:
https://developer.android.com/studio/publish/app-signing
https://support.google.com/googleplay/android-developer/answer/10144311
https://support.google.com/googleplay/android-developer/answer/14151465
https://support.google.com/googleplay/android-developer/answer/13849271
https://support.google.com/googleplay/android-developer/answer/9866151
https://developer.android.com/guide/practices/page-sizes
