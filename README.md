<p align="center">
  <img src="https://img.shields.io/badge/Android-Native-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-BOM_2024.09-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Room-2.7-669DF6?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room" />
  <img src="https://img.shields.io/badge/Gemini_AI-3.5_Flash-1E88E5?style=for-the-badge&logo=googlegemini&logoColor=white" alt="Gemini AI" />
</p>

<p align="center"><b>A modern, fast, and offline-first job application tracker — with Gemini AI parsing, cover letters, and interview reminders.</b></p>

<p align="center">Dashboard • AI job parser • Cover-letter generator • Follow-up emails • Interview reminders • Export & auto-backup</p>

---

## Table of Contents

- [About the Project](#about-the-project)
- [Why I Built JobTracker](#why-i-built-jobtracker)
- [Who Will Use It](#who-will-use-it)
- [Why They Will Use It](#why-they-will-use-it)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Security](#security)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)
- [Author & Contact](#author--contact)

---

## About the Project

**JobTracker** is a production-oriented, offline-first Android app for tracking **every job you apply to — from first application to offer**.

Most job seekers track applications in spreadsheets that go stale, lose interview dates in chat threads, or rewrite the same cover letter from scratch. JobTracker brings everything together in one clean, fast, private place:

> Status-based dashboard, full application details with recruiter + interview info, Gemini AI job-description parser, tailored cover-letter and follow-up email generation, interview alarms with reboot recovery, auto-save drafts, and one-tap CSV/JSON/Markdown export with local auto-backup — all stored locally in Room, no account required.

Built with **Kotlin + Jetpack Compose (Material 3)**, **Room 2.7**, **Navigation Compose**, and **Gemini API (gemini-3.5-flash with automatic model failover)**, following Clean Architecture + MVVM.

Status: **v1.0.0 — Working end-to-end.** The app builds and runs on emulator and physical device (see [Getting Started](#getting-started) before running).

---

## Why I Built JobTracker

I built JobTracker to solve problems I saw in my own job hunt firsthand:

1. **Spreadsheets don't hunt jobs:** Company, role, salary, recruiter, referral, interview date/time/type, notes — a real application has 15+ fields. Generic notes apps model none of this. JobTracker is application-first: statuses, interviews, recruiters, reminders.
2. **Pasting a JD should fill the form:** Copy-pasting a LinkedIn/BDJobs posting and retyping company, title and salary is wasted time. The Gemini AI parser extracts company, title, salary and key points into structured JSON and pre-fills the form, with regex fallback if the model returns messy text.
3. **Writing friction kills consistency:** Cover letters and thank-you emails take 30 minutes each, so people skip them. One tap generates a tailored cover letter + talking points and a post-interview follow-up with subject line from the actual application context.
4. **Missed interviews cost offers:** An interview date in a spreadsheet doesn't remind you. Reminders are scheduled with AlarmManager, survive reboot via `BootCompletedReceiver`, and interviews can be pushed to Calendar in one tap.
5. **Your data must survive:** Uninstalls, cleared data, dead phones. Every change triggers a local JSON auto-backup (`job_tracker_autobackup.json`) with auto-restore on empty DB, plus manual CSV/JSON/Markdown share — no cloud lock-in, nothing silently lost.

This is also my exploration of production-grade Android engineering: Clean Architecture, MVVM, Room + Flow, Coroutines, Compose navigation, AI failover, notifications, and screenshot/unit testing.

---

## Who Will Use It

- **Active job seekers** tracking 10–100+ applications across LinkedIn, BDJobs, referrals and company portals.
- **Fresh graduates** who need cover letters and follow-up emails without knowing the format by heart.
- **Career switchers** juggling interviews, recruiter contacts and referral sources in one timeline.
- **Freelancers / contract engineers** tracking proposals, clients and follow-ups like a sales pipeline.
- **Anyone privacy-conscious** who wants a fully local tracker with no signup, no server, no sync account.

No login required — install, add your first application, done.

---

## Why They Will Use It

- **Dashboard built for speed:** Status summary (Applied / Interviewing / Offer / Rejected / Drafts), upcoming-interviews card, search-as-you-type, status filter, and 4 sort orders — find any application in seconds.
- **AI that actually fills forms:** Paste any job posting → company, title, salary and highlights extracted and mapped into the add/edit form, not just dumped as text.
- **One-tap writing assistant:** Tailored 3-paragraph cover letter + 3 talking points, and polished follow-up/thank-you emails with subject line — copy or share directly.
- **Interviews you can't miss:** Date + time + reminder-minutes, system notification via `InterviewReminderReceiver`, calendar insert, reboot-safe rescheduling.
- **Never lose a draft:** Unsaved add-form state auto-persists via `JobDraftManager` and resurfaces as a resumable draft section on the dashboard.
- **Export anywhere:** CSV for spreadsheets, JSON for backup/migration, Markdown summary report grouped by status — share, copy to clipboard, or restore later.

---

## Key Features

### 1. Dashboard & Tracking
- Status summary card: total, applied, interviewing, offer, rejected, drafts
- Upcoming interviews card sorted by date
- Search (company, title, recruiter, notes), status filter chips, sort (Newest / Oldest / Company A–Z / Title A–Z)
- Job cards with status badge, salary, date applied, interview meta; swipe-safe edit/delete
- Empty states per filter so zero-results never look broken

### 2. Add / Edit Applications
- Full model: company, title, date applied, status, salary, notes, recruiter name/email/phone, referral source
- Interview block: date, time, type (Onsite / Remote / Phone / etc.), notes, reminder toggle + minutes-before
- Draft auto-save: leaving the form mid-typing preserves everything in SharedPreferences and restores it later
- Validation: company + title required; dates normalized to `yyyy-MM-dd`

### 3. Gemini AI Tools
- **Job-description auto-parser:** raw posting text → `{ companyName, jobTitle, salary, keyPoints, detectedStatus }` JSON, markdown-fence stripped, regex fallback per field
- **Cover-letter generator:** company + role + JD/notes + candidate highlights → cover letter + 3 talking points
- **Follow-up email drafter:** application + intent (e.g. Post-Interview Thank You) → subject + body
- Resilient calling: `gemini-3.5-flash` → `gemini-3.1-flash-lite-preview` → `gemini-flash-latest` → `gemini-3.1-pro-preview` failover on 404/429/5xx, 60s timeouts, clean `Result<T>` errors

### 4. Interviews & Reminders
- AlarmManager reminders at `interviewDateTime - minutesBefore` (default 15 min)
- `InterviewReminderReceiver` posts the notification; `BootCompletedReceiver` re-schedules all future reminders after reboot
- One-tap Add-to-Calendar with title, description, recruiter and 1-hour block
- Time handling kept as plain `yyyy-MM-dd` + `HH:mm` strings so sorting and display stay predictable

### 5. Export, Backup & Restore
- Export helpers (`JobExportHelper`): RFC-safe CSV, pretty JSON (`exportDate` + `totalApplications` + items), grouped Markdown summary report
- Share via system sheet, copy to clipboard, calendar intent
- `LocalAutoBackupManager`: persistent `job_tracker_autobackup.json` in multiple device locations + metadata prefs; auto-restore when DB is empty; import from file/URI

### Cross-cutting
- 100% offline: Room is source of truth, AI is the only network call and degrades gracefully without a key
- Material 3 dynamic theme, dark-mode ready (`ui/theme/`)
- Bottom-sheet AI panel so the cashier — I mean, the applicant — never leaves context

---

## Tech Stack

**App:**
- Kotlin 2.2.10, AGP 9.1.1, compile/target SDK 36, minSdk 24, Java 11
- Jetpack Compose (BOM 2024.09.00), Material 3 + Material Icons Extended, Activity Compose, Navigation Compose 2.8.9
- Lifecycle Runtime Compose + ViewModel Compose, Coroutines 1.10.2
- Room 2.7.0 (KTX + Runtime + KSP compiler), Moshi 1.15.2 + codegen, OkHttp 4.10.0, Retrofit 2.12.0 + Moshi converter
- Firebase BOM 34.17.0: Firebase AI + App Check (Recaptcha + Debug)
- Secrets Gradle Plugin 2.0.1 (`.env` → `BuildConfig.GEMINI_API_KEY`), Google Services (passthrough, `google-services.json` optional)

**Infrastructure & Quality:**
- Gradle 8.x, KSP, Foojay toolchains, parallel + caching + configuration-cache enabled
- Turbine 1.2.0, Mockito-Kotlin 5.4.0, Robolectric 4.16.1, Roborazzi 1.59.0 (screenshot tests), JUnit 4.13.2, Espresso 3.7.0
- Manual QA on emulator + physical device; no CI yet (see [Roadmap](#roadmap))

See `gradle/libs.versions.toml`, `app/build.gradle.kts` and `gradle.properties` for full versions.

---

## Architecture Overview

Compose screens → ViewModels → UseCases → Repository → Room / GeminiService:

- `domain/model` — `JobApplication`, `JobStatus` (Applied/Interviewing/Offer/Rejected), `JobSortOption`, `StatusSummary`; no Android dependencies
- `domain/repository` — `JobRepository` interface (Flow-based queries, CRUD, summary)
- `domain/usecase` — `GetJobApplications`, `GetJobById`, `SaveJobApplication`, `DeleteJobApplication`, `GetStatusSummary`, `ParseJobDescription`, `GenerateCoverLetter`, `GenerateFollowUpEmail`
- `data/local` — `JobDatabase`, `JobApplicationDao`, `JobApplicationEntity` + `JobMapper`
- `data/remote` — `GeminiService` (OkHttp + `org.json`, direct `generativelanguage.googleapis.com` calls)
- `data/repository` — `JobRepositoryImpl` (DAO + auto-backup trigger)
- `presentation/` — `dashboard/` (screen + ViewModel + UiState + components), `detail/` (add/edit screen + ViewModel), `ai/` (bottom sheet + ViewModel), `export/`, `navigation/` (`Screen` sealed class)
- `di/` — `AppContainer` / `DefaultAppContainer` (manual DI, lazy singletons)
- `util/` — `JobDraftManager`, `LocalAutoBackupManager`, `JobExportHelper`, `InterviewNotificationHelper`
- `receiver/` — `InterviewReminderReceiver`, `BootCompletedReceiver`

Data flow: `DashboardScreen → DashboardViewModel → GetJobApplicationsUseCase → JobRepositoryImpl → Room DAO → Flow<List<JobApplication>> → UI recomposes`. AI flow: `AiToolsBottomSheet → AiToolsViewModel → Parse/Generate UseCase → GeminiService → Result → UiState`. The UI never touches DAO or network directly.

---

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (Ladybug or newer recommended)
- JDK 17 for the IDE (app targets Java 11 bytecode)
- An emulator (API 29+) or physical device with USB debugging
- A [Gemini API key](https://aistudio.google.com/app/apikey) (only needed for AI features — tracking works without it)

### Run Locally

1. Clone the repo:
   ```bash
   git clone https://github.com/InfinityAbir/job-tracker.git
   ```
2. Open Android Studio → **Open** → select the `job-tracker` directory. Allow it to sync and fix incompatibilities on import.
3. Create `.env` from the example and add your key:
   ```bash
   cp .env.example .env
   # then edit .env:
   # GEMINI_API_KEY=AIzaSy...your-key
   ```
4. If Gradle complains about `debug.keystore` (custom `debugConfig` points at `${rootDir}/debug.keystore`), either let Android Studio generate one there or temporarily remove this line from `app/build.gradle.kts`:
   ```kotlin
   debug { signingConfig = signingConfigs.getByName("debugConfig") }
   ```
   Standard debug builds work without it.
5. Run on an emulator or physical device (▶ Run `app`).

| What | Where |
| --- | --- |
| App | Launcher → **Job Tracker** |
| Local DB | Room `job_tracker_db` (private app storage) |
| Auto-backup | `job_tracker_autobackup.json` (app files + external files) |
| Drafts | SharedPreferences `job_tracker_drafts` |

### Build and Test

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest   # needs emulator/device
```

AI Studio origin: this project was scaffolded in AI Studio — see `metadata.json`. The `View your app in AI Studio` link in the old README is stale for forks; use the steps above.

---

## Configuration

- **Environment:** Copy `.env.example` → `.env` and set `GEMINI_API_KEY`. The Secrets plugin injects it as `BuildConfig.GEMINI_API_KEY`; `GeminiService` falls back to the `GEMINI_API_KEY` process env var. Never commit `.env` (already in `.gitignore`).
- **AI models:** Priority order in `GeminiService.candidateModels` — `gemini-3.5-flash`, `gemini-3.1-flash-lite-preview`, `gemini-flash-latest`, `gemini-3.1-pro-preview`. Timeouts 60s connect/read/write; 350 ms backoff on failover.
- **Firebase:** `google-services.json` is optional (`missingGoogleServicesStrategy = WARN`, `googleServices.missing.passthrough=true`). App Check Recaptcha + Debug flavors included; add the json only if you enable Firebase features.
- **Database:** Room auto-migrates destructively in dev; version bumps need a proper `Migration` before release. Backup file name and prefs keys live in `LocalAutoBackupManager`.
- **Signing:** Release uses `KEYSTORE_PATH` / `STORE_PASSWORD` / `KEY_PASSWORD` env vars (default `${rootDir}/my-upload-key.jks`, never committed). Debug uses local `debug.keystore` (git-ignored).

---

## Security

| Control | How |
|---|---|
| API keys | `GEMINI_API_KEY` in host `.env` only → `BuildConfig`; `.env` + `debug.keystore` + `*.jks` git-ignored; never logged, never committed |
| Transport | Gemini calls over HTTPS POST to `generativelanguage.googleapis.com`; key sent as query param per Google API convention, never stored in DB |
| App integrity | Firebase App Check (Recaptcha in release, Debug in dev) wired for future backend calls |
| Local data | Room DB in private app storage; backups in app-scoped storage; export/share is explicit user action only |
| Reminders | Exact-alarm permission requested only for interview notifications; reboot receiver re-registers future alarms only |
| Error handling | AI failures surface as `Result.failure` with user-readable message; no stack traces or keys in UI |

---

## Project Structure

```
job-tracker/
├── app/
│   ├── build.gradle.kts            # application, SDKs, signing, secrets, dependencies
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/
│           ├── MainActivity.kt / JobTrackerApp.kt
│           ├── di/AppContainer.kt              # manual DI graph
│           ├── domain/
│           │   ├── model/JobApplication.kt     # JobApplication, JobStatus, JobSortOption
│           │   ├── repository/JobRepository.kt
│           │   └── usecase/JobUseCases.kt / AiJobUseCases.kt
│           ├── data/
│           │   ├── local/ (JobDatabase, Dao, Entity)
│           │   ├── mapper/JobMapper.kt
│           │   ├── remote/GeminiService.kt     # parser + cover letter + follow-up
│           │   └── repository/JobRepositoryImpl.kt
│           ├── presentation/
│           │   ├── dashboard/ (Screen, ViewModel, UiState, components/)
│           │   ├── detail/ (AddEditJobScreen, ViewModel, UiState)
│           │   ├── ai/ (AiToolsBottomSheet, ViewModel, UiState)
│           │   ├── export/ExportBackupBottomSheet.kt
│           │   └── navigation/Screen.kt
│           ├── receiver/ (InterviewReminderReceiver, BootCompletedReceiver)
│           ├── util/ (JobDraftManager, LocalAutoBackupManager, JobExportHelper, InterviewNotificationHelper)
│           └── ui/theme/ (Color, Type, Theme)
├── gradle/libs.versions.toml
├── gradle.properties / settings.gradle.kts / build.gradle.kts
├── .env.example / .gitignore
├── metadata.json
└── README.md
```

---

## Roadmap

- [ ] Firestore sync + Google Sign-In (optional cloud backup across devices)
- [ ] Stats charts (applications/week, response rate, pipeline funnel)
- [ ] Home-screen widget: upcoming interviews + application counts
- [ ] CSV/JSON import with duplicate detection and field mapping preview
- [ ] Resume PDF attach + per-application document vault
- [ ] Reminder channels (exact vs. inexact alarms) + snooze actions
- [ ] GitHub Actions CI: lint + unit tests + debug APK on PR
- [ ] Play Store release (versioned changelog, staged rollout, crash reporting)

Contributions and issue reports are welcome.

---

## Contributing

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Follow the architecture: UI in `presentation/`, business rules in `domain/usecase`, I/O in `data/` — never call DAO or OkHttp from a Composable
4. Add/extend tests for business rules (JUnit + Turbine) and UI regressions (Roborazzi) where it matters
5. Commit: `git commit -m "Add your feature"`
6. Push and open a Pull Request

Please keep string resources clean, never commit `.env` / keystores / `google-services.json`, and include screenshots for UI changes.

---

## License

This project is currently **All Rights Reserved** — source available for learning and review.

If you want to reuse code or contribute a licensed fork, please contact the author. A formal open-source license (e.g., MIT) can be added on request.

---

## Author & Contact

**Abir Hasan (InfinityAbir)** — ASP.NET Core / Android developer, building clean, secure, real-world apps.

- GitHub: https://github.com/InfinityAbir
- Portfolio: https://infinityabir.github.io/abir-hasan-portfolio/
- LinkedIn: https://www.linkedin.com/in/infinityabirhasan/
- Email: abirha3896@gmail.com

> If JobTracker helps your job hunt stay organized, please ⭐ star the repo and share feedback via Issues.
