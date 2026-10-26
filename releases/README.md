# Current release: HisabKitab

The signed offline production APK, Play bundle, privacy page and store artwork are in [HisabKitab/](HisabKitab/). See [release instructions](../docs/HISABKITAB-RELEASE.md). The MiniLedger APK below is an older development preview.

# MiniLedger Android preview

File: **MiniLedger-Preview-v1.0.apk** (Android 7.0 or newer).

This is the signed debug APK of the Kotlin/Compose design preview installed during development. It includes the white content surfaces, aligned headers, full-image profile header, loan-details bottom sheet and functional settings pages with CSV export. It is not the React Native/PostgreSQL assignment build and is not a production release.

## Try the preview

1. Download the APK on an Android phone and open it.
2. If prompted, permit this installation from your browser or file manager.
3. Open **MiniLedger Preview**, enter a phone number and request the mock OTP. On the verification screen, tap the text **“Check the server console for the OTP (this is a mock OTP)”** to fill the generated demo code, then tap Verify OTP. No SMS is sent. Alternatively, `adb logcat -s MiniLedgerServer` shows the local mock code.
4. Verify the code, add a loan, view outstanding balances and mark a loan repaid. Preview loans are stored locally on that phone, not in the assignment PostgreSQL database.

The APK contains no developer loan records. Existing installations retain their own records when updated. It has been verified with Android apksigner; SHA256SUMS.txt contains its checksum.

## Review the actual assignment

The required React Native implementation is in **mobile/** and the Node.js/TypeScript/PostgreSQL implementation is in **backend/**. Use the root README and docs/VIDEO-DEMO.md to run and review that implementation through Expo Go. The computer's development API is not publicly hosted, and localhost/USB forwarding does not work on a remote reviewer's phone without their own setup.

## Publish the downloadable APK on GitHub

Push your source code, then use your repository's **Releases → Draft a new release**. Set a tag such as `v1.0-preview`, attach MiniLedger-Preview-v1.0.apk and SHA256SUMS.txt, and mark it as a pre-release. Include the distinction above in the release notes. Reviewers download the APK under the release's Assets section.

Do not upload .env files, signing keystores, backend/.postgres, backend/data, output logs or node_modules. Attach APKs as release assets instead of committing generated binaries to source history.

GitHub instructions: https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository
