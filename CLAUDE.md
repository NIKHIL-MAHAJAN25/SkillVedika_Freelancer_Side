# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

SellerApp — a single-module native Android app (Kotlin, View Binding, no Compose) for a freelancer/client
marketplace ("skill-vedika"). Package root: `com.nikhil.sellerapp`.

## Build & test commands

Run from the repo root (Windows: use `gradlew.bat`, or `./gradlew` in Git Bash).

- Build debug APK: `./gradlew assembleDebug`
- Build release APK (requires signing keys in `local.properties`, see below): `./gradlew assembleRelease`
- Unit tests: `./gradlew testDebugUnitTest`
- Single unit test: `./gradlew testDebugUnitTest --tests "com.nikhil.sellerapp.SomeTest"`
- Instrumented tests (needs a connected device/emulator): `./gradlew connectedAndroidTest`
- Lint: `./gradlew lintDebug`

Note: `app/src/test` and `app/src/androidTest` currently only contain the default
`ExampleUnitTest`/`ExampleInstrumentedTest` stubs — there is no real test suite yet.

### Required local config

`app/build.gradle.kts` reads secrets from `local.properties` (gitignored) via `buildConfigField`/signing config.
A build will succeed with empty strings if these are missing, but network calls and release signing will fail:

- `SUPABASE_URL`, `SUPABASE_KEY` — Supabase project (used for Storage only, see below)
- `BRANDFETCH_API_KEY` — Brandfetch logo API
- `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` — release signing config

## Architecture

### Backends — three separate services, each with a distinct purpose

1. **Firebase (Auth + Firestore)** is the primary datastore for user/profile/chat data. Firestore offline
   persistence is enabled in the `Application` class. There is no repository layer — screens call
   `FirebaseFirestore.getInstance()` / `FirebaseAuth.getInstance()` directly.
2. **Supabase** (`io.github.jan-tennert.supabase`, pinned to **2.6.1**) is installed with only the `Storage`
   module — used for file/image storage, not auth or DB. Pinned because 3.0+ forces Ktor 3, which breaks
   compatibility with the Ktor-2-based setup used elsewhere (see comments in `app/build.gradle.kts`).
3. **Custom backend** at `https://skill-vedikaser.vercel.app/` (`mailretro/Retromail` + `Mailfetch` Retrofit
   interface) handles: sending welcome/OTP emails, and proxying resume-vs-job-description analysis to Gemini
   (`analyze-resume` endpoint — request/response DTOs live in `GeminiClient/`). The app never calls the
   Gemini API directly.
4. **Brandfetch** (`brandfetch/RetroBrand` + `Brandfetch`) is a fourth, independent Retrofit client used only
   for fetching company logos.

Each of Retromail/RetroBrand is its own `object` with a lazily-built `Retrofit` instance — there is no shared
network client between them (Retromail's has an `HttpLoggingInterceptor` + 30s timeouts; RetroBrand's does not).

### App entry / routing flow

`comprofile/supabasefile.kt` — despite its name, this is the app's `Application` class
(`android:name=".comprofile.supabasefile"` in the manifest). It initializes Firestore persistence, PDFBox, and
the Supabase client on `onCreate`.

`MainActivity` is the splash/router: after a Lottie splash animation it reads the current `FirebaseAuth` user,
looks up their `Users/{uid}` Firestore document, and routes based on two boolean fields:

- no user → `SignUpActivity`
- `profilecomplete == false` → `SignUpActivity`
- `profilecomplete == true` but `approved == false` → `Entercode` (awaiting admin approval / OTP)
- both true → `HomeActivity`

`adminside/ApprovalScreen` is where an `ADMIN`-role user reviews and approves pending `FREELANCER`/`CLIENT`
accounts (the `approved` flag above).

`HomeActivity` hosts a single `NavHostFragment` (`res/navigation/nav_graph.xml`) with a bottom navigation bar.
Fragments for `search`/`message`/`order` show the bottom nav; all other destinations (profile sub-screens,
Gemini resume checker, chat thread, project display, etc.) hide it — see the `addOnDestinationChangedListener`
in `HomeActivity`.

### Code organization: package-per-feature, not layer-per-type

There is no `viewmodel`/`repository`/`network` split. Each feature package under
`app/src/main/java/com/nikhil/sellerapp/` bundles its own Activity/Fragment + RecyclerView Adapter(s) + any
feature-local DTOs together, e.g. `Chatting`, `ClientProfileView`, `experience`, `qualification`, `certificate`,
`servicemapping`, `skills`, `profilepage`. Shared Firestore-mapped models live centrally in `dataclasses/`
(`User` — with `UserRole` enum `CLIENT`/`FREELANCER`/`ADMIN` — `Freelancer`, `Client`, `Project`, `Chat`,
`Message`, `Review`, `Experience`, `Qualification`, `Certification`).

`Utils/Extensions.kt` holds the shared Kotlin extensions used throughout the app: `Activity.snack()` /
`Fragment.snack()` for Snackbars, `loge`/`logd` for tagged logging, and `Navigateto`/`Navigatetoclear` for
starting activities (the latter clears the back stack — used for auth/onboarding transitions).

### Build config notes

- View Binding is enabled (`viewBinding = true`); no Jetpack Compose in this project.
- `kapt` is used for Glide's annotation processor.
- Release builds are minified + resource-shrunk with `proguard-rules.pro`; baseline profiles live under
  `app/release/baselineProfiles/` (generated artifacts, tracked in git).
