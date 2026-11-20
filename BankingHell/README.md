# BankingHell

Northstar Bank is an Android banking simulation. On first launch, create a four-digit PIN. The home screen includes account activity, documents, statements, and offers.

## Build

Open the project in Android Studio and build the `app` module, or run `gradle :app:assembleDebug`.

The included SDK build script is an alternative:

```bash
bash scripts/build-local.sh
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The script requires Android SDK platform 34, build-tools 35.0.0, a JDK, `zip`, and `keytool`. Set `ANDROID_SDK_ROOT` when the SDK is installed in a different location.
