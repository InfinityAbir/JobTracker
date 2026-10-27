<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/893dd2a1-515c-42df-9ef3-c2a889a63676

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. AI tools need a free Gemini API key from https://aistudio.google.com/apikey. The key is entered inside the app (AI sheet) and kept only on the device; nothing is compiled into the APK.
5. Run the app on an emulator or physical device. Debug builds work out of the box: they use `debug.keystore` at the project root when present, otherwise the standard Android debug keystore.

## Build a Signed Release

Release builds are signed with the production keystore at `E:/Keys/jobtracker-upload.jks`. Set the passwords via environment variables (`STORE_PASSWORD`, `KEY_PASSWORD`) to build the signed release variant.

```bash
export STORE_PASSWORD="your-keystore-password"
export KEY_PASSWORD="your-key-password"

# Build an AAB (recommended for Google Play)
./gradlew bundleRelease

# Build an APK
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/` or `app/build/outputs/bundle/release/`.

If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
