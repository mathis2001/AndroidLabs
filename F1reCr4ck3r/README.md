# F1reCr4ck3r

F1reCr4ck3r is a fictional motor-insurance Android CTF lab. The player brief is in [SCENARIO.md](SCENARIO.md).

## Build

Open this directory in Android Studio or run `bash scripts/build-local.sh`. The project uses Android SDK 34 and JDK 17. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The Firebase Android client file at `app/google-services.json` is included so the app can connect to the lab's Firebase project. It contains client identifiers, not administrator credentials. The Realtime Database rules used by the lab are in [firebase-rules.json](firebase-rules.json). Local SDK settings and build outputs are excluded from Git.

The customer demo account is `alex.morgan@example.com` with policy PIN `2468`. Network-dependent features require the lab's Firebase services to be available.
