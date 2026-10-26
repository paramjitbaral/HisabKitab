# Release validation — October 26, 2026

- Signed release APK and AAB built successfully; release Android lint completed with no errors. Existing deprecation/unused-resource warnings remain.
- APK signature verified with Android apksigner. Package: com.paramjitbaral.hisabkitab; version 1.0.0 (2); Android 7.0+, target SDK 36.
- APK 16 KB ZIP alignment passed. Bundled native libraries were inspected for 16 KB ELF load-segment alignment. A 16 KB-page device run has not been performed; run Play pre-launch/device tests before Play publication.
- 13 Android instrumentation tests passed on a connected Android 16 phone: backup round-trip and invalid-file rejection, transactional restore/deduplication, repayment and deletion in an isolated database, loan entry, summary/navigation, history filtering, profile settings, onboarding, repayment confirmation and CSV escaping, plus screenshot capture.
- Final follow-up changes corrected Settings text encoding and the About version/developer/support labels; release build and lint passed again.
- Production installation opens onboarding and the dashboard without phone login, OTP or backend.
- Production manifest contains no Internet permission. Ads, analytics and remote authentication SDKs are not included.
- Store screenshots use fictional sample records, not the device owner's loans.
- Original React Native assignment typecheck and lint passed; that separate client/backend is not included in the offline native APK.

This is a local release validation record, not Google Play approval. Public privacy hosting, account declarations, required testing and Play review remain with the publisher.
