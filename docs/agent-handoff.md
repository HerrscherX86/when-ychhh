# When Yah — Agent Handoff

> Snapshot date: 2026-11-14
> Git branch: `main`
> HEAD: `5d94cda` — `Update v1.2 2nd half`
> Remote: `https://github.com/HerrscherX86/when-ychhh.git`
> App version: `versionCode = 4`, `versionName = "1.3"`
> Last verified: `:app:assembleDebug`, `:app:testDebugUnitTest` (100 tests), `:app:lintDebug` (0 errors, 11 warnings)

This document is the detailed handoff for another coding agent. `AGENTS.md` remains the concise always-loaded guide; this file records the current state, history, decisions, and known gaps.

---

## A0. v1.3 status (2026-11-14)

v1.3 “Daily-Use Polish” is **implemented in the working tree but not committed or published**. The
v1.2 sections below remain accurate as the baseline history; this section records what changed.

Implemented:

- **Home-screen widgets** in `data/widget/`:
  - `WhenYahWidgetBase` loads a shared `WidgetSnapshot` from `WidgetSnapshotFactory` (totals,
    progress, active/funded counts, closest goal, ready-to-buy list); covered by
    `WidgetSnapshotFactoryTest`.
  - Three styles: `WhenYahWidget` (overview), `ClosestGoalWidget`, and `ReadyToBuyWidget`. Each has
    its own receiver and `res/xml/*_info.xml`.
  - Widget copy comes from `SettingsRepository.currentSettings().language`, not the process
    `LanguageStore`.
  - `WidgetUpdater.refresh()` updates all three widgets after item/ledger/purchase/restore/backup
    writes, on app start, and after language changes. Failures are swallowed so a widget problem
    cannot fail a savings write.
  - Settings → Appearance can pin any style through `WhenYahWidgetType`.
- **Notification quick-deposit action**:
  - `NotificationService` adds an “Add savings” action to plan reminders and opens the item detail
    with the deposit dialog pre-filled with the plan contribution.
  - Notification bodies open the item detail; goal notifications open the item; summaries/tests open
    Home.
  - `LaunchContract` + `LaunchRequestParser` (pure) + `AppLauncher` route intents into
    `WhenYahApp`. `MainActivity` is `singleTop` and handles cold-start intents only when
    `savedInstanceState == null`.
  - `Routes.ITEM_DETAIL` now accepts an optional `depositAmount` long arg (0 = no dialog);
    `ItemDetailViewModel` consumes it and removes it from `SavedStateHandle`.
- **Adaptive layout**:
  - `NavigationRail` at ≥600dp on top-level routes; bottom bar remains on phones.
  - `ui/components/Adaptive.kt` caps content width (Home 960dp, Settings 840dp, detail/forms 720dp).
  - Collections, collection detail, and Archive use adaptive `LazyVerticalGrid`s.
  - `rememberFabVisibility` supports both list and grid states.
- **Predictive back**: `android:enableOnBackInvokedCallback="true"`; pop transitions remain fades.
- **Dependencies**: Glance `1.2.0`, `glance-material3`, Compose Material3 Adaptive `1.3.0`.
  Glance pulls WorkManager transitively; reminders still use `AlarmManager`.

Verification:

- `:app:assembleDebug` — success.
- `:app:testDebugUnitTest` — **100 tests**, 0 failures.
- `:app:lintDebug` — 0 errors, 11 warnings.
- Device/emulator checks are **pending** for the widget, notification action, predictive back, and
  tablet/foldable layout. See `docs/ROADMAP_V1.3.md` for the checklist.

Known v1.3 limitations:

- Widgets can lag briefly after a write because Glance schedules updates through WorkManager.
- The widget uses the default Material 3 palette, not the app’s custom accent/dynamic color.
- Two-pane tablet list-detail is intentionally out of scope.
- Predictive back must be verified on an API 36 device before release; the manifest flag is the only
  change and can be reverted if the preview regresses.

Next steps:

1. Run the v1.3 manual verification matrix on a phone and a tablet/resizable emulator.
2. Commit the working tree.
3. Build/sign v1.3 and publish the GitHub release with `update.json`.
4. Verify v1.2 → v1.3 update detection and data preservation.

---

## A. Project Overview

When Yah is a native Android wishlist and savings tracker. It is designed for personal, offline-first use and is distributed through GitHub Releases rather than Google Play.

Core idea: record items you want to buy, set an IDR target price, manually record money set aside for each item, create a daily/weekly/monthly savings projection, and track progress toward the goal. Purchases are recorded against the existing savings ledger and moved to an archive. All data stays local.

Main constraints:

- **IDR only.** There is no currency picker, FX conversion, or currency network call.
- **Offline-first.** The only network request is the GitHub update manifest.
- **No backend, account, login, bank/payment integration, or cloud sync.**
- **No actual funds are held or transferred.** The app records a plan and ledger history.
- Minimum Android: **API 31 / Android 12**.
- UI languages: **English** and **Indonesian** (Indonesian uses a friendly Gaul register).
- Product source of truth: `wishlist_savings_tracker_PRD_v1.2.md`. Code behavior takes precedence over the README when they disagree.

Current high-level objective: v1.3 (“Daily-Use Polish”) is implemented in the working tree but not committed or published. See §A0 for the v1.3 summary; the v1.2 Track A/B work is committed and released.

---

## B. Technology Stack

### Build and language

| Item | Value |
|---|---|
| Project type | Single-module Android app, `:app` |
| Language | Kotlin 2.2.10 |
| Build | Gradle wrapper 9.4.1, AGP 9.2.1 |
| Java target | Java 11 (`sourceCompatibility`/`targetCompatibility`) |
| Gradle JVM toolchain | Java 21 (`gradle/gradle-daemon-jvm.properties`) |
| Compile SDK | 37 |
| Target SDK | 36 |
| Min SDK | 31 |
| Package / application ID | `id.my.f1zz.whenyah` |
| Compose | Compose BOM `2026.02.01`, Material 3, material-icons-extended |
| DI | Hilt 2.60.1, KSP 2.3.6 |
| Database | Room 2.8.5 |
| Preferences | Preferences DataStore 1.3.0 |
| Images | Coil 3.3.0 |
| Serialization | kotlinx-serialization-json 1.9.0 |
| Navigation | Navigation Compose 2.10.2 |
| Lifecycle | lifecycle 2.11.0, activity-compose 1.13.0 |

AGP 9 uses built-in Kotlin. Do **not** apply `org.jetbrains.kotlin.android` or kapt. KSP and Hilt are already configured.

Coil is pinned to 3.3.0 and kotlinx-serialization to 1.9.0 because newer releases pull Kotlin 2.3/2.4 metadata that this project’s Kotlin 2.2 compiler cannot read. Do not bump those casually.

### Android components and permissions

Manifest permissions:

- `INTERNET` — only for the update manifest.
- `POST_NOTIFICATIONS`
- `RECEIVE_BOOT_COMPLETED`
- `SCHEDULE_EXACT_ALARM`

Application class: `WhenYahApplication` (`@HiltAndroidApp`).

Receivers:

- `ReminderAlarmReceiver` — handles plan and summary alarms.
- `ReminderRescheduleReceiver` — reschedules after boot, app update, and exact-alarm permission state changes.

There is no WorkManager dependency; it was removed in favor of AlarmManager. There is no OkHttp/Retrofit/server client. The update checker uses `HttpURLConnection`.

### Not present

- No `package.json`, Node/npm, or JavaScript toolchain.
- No CI workflow, pre-commit hook, custom lint config, or license file.
- No backend, API routes, server actions, authentication, authorization, or RLS.
- No environment files/secrets in the repo. `local.properties` exists locally and is gitignored; it only contains the local Android SDK path.
- `.opencode/skills/` contains generic design skills (banner/brand/design/slides/ui-styling/ui-ux-pro-max), not app runtime code. There is no project-specific OpenCode configuration beyond `AGENTS.md`.

### Test setup

- Host-side JUnit 4 tests in `app/src/test`.
- Instrumentation scaffold only in `app/src/androidTest` (`ExampleInstrumentedTest`).
- Compose UI test dependencies are present but there are no real UI tests yet.

---

## C. Architecture

### Layers

```text
Compose UI (ui/)
   ↓ StateFlow / events
ViewModels (ui/*ViewModel.kt)
   ↓ suspend calls / flows
Repositories (data/repository/, data/backup/, data/settings/, data/update/)
   ↓
Room DAOs + Preferences DataStore + filesDir images
   ↑
Pure domain calculators (domain/)
```

Hilt provides the application, database, DAOs, repositories, notification/update managers, and settings. Screen ViewModels are `@HiltViewModel`.

### Package map

```text
app/src/main/java/id/my/f1zz/whenyah/
├── MainActivity.kt              # activity, onboarding gate, theme/language, update dialog
├── WhenYahApplication.kt        # Hilt app, notification channels, language + schedule sync
├── navigation/                  # Routes.kt, TopLevelDestination.kt
├── di/DatabaseModule.kt         # Room builder + DAO providers
├── data/
│   ├── local/
│   │   ├── entity/              # Room entities
│   │   ├── dao/                 # DAOs
│   │   ├── model/               # CollectionWithItemCount
│   │   ├── WhenYahDatabase.kt
│   │   ├── DatabaseConstants.kt # DB version = 2
│   │   ├── Migrations.kt        # MIGRATION_1_2
│   │   └── Converters.kt
│   ├── repository/              # Wishlist, Savings, Plan, Purchase
│   ├── image/ItemImageStore.kt  # copies images into filesDir/item_images
│   ├── backup/                  # ZIP export/restore + validation
│   ├── settings/SettingsRepository.kt
│   ├── notification/            # NotificationService, scheduler, receivers, coordinator
│   └── update/                  # UpdateChecker/Manager/Preferences/Manifest
├── domain/
│   ├── savings/                 # ledger balance, summary
│   ├── purchase/                # spend-all accounting
│   ├── plan/                    # projections, recurrence
│   ├── dashboard/               # dashboard aggregation
│   ├── notifications/           # next-occurrence calculations
│   ├── update/                  # prompt/snooze/skip policy
│   ├── validation/              # IDR/name/URL validation
│   ├── settings/                # AppSettings + enums
│   ├── format/                  # IDR formatter
│   └── i18n/                    # AppStrings, English/Indonesian, LanguageStore
├── ui/
│   ├── WhenYahApp.kt            # Scaffold, routes, navigation transitions, snackbar host
│   ├── home/                    # Home, Activity
│   ├── collections/             # Collections list/detail
│   ├── item/                    # Item detail/editor, transaction history, plan editor
│   ├── archive/                 # Purchased archive/detail
│   ├── settings/                # Settings, backup UI
│   ├── onboarding/              # first-launch pager
│   ├── components/              # cards, dialogs, AppFeedback, update dialog, FAB helper
│   ├── navigation/              # LocalSetTopBarBackHandler
│   ├── theme/                   # color, type, theme, AppMotion
│   └── util/                    # formatters, IDR visual transformation
└── ...
```

### Navigation

`WhenYahApp.kt` uses Navigation Compose with a shared `Scaffold` (top bar, bottom bar, snackbar host).

Top-level routes:

- `home`
- `collections`
- `settings`

Detail routes (`navigation/Routes.kt`):

```text
collection/{collectionId}
item/{itemId}
item/{itemId}/history
item/{itemId}/plan
add-item?collectionId={collectionId}
edit-item/{itemId}
archive
archive/{itemId}
activity
```

Detail routes use fades; top-level tab switches use a shorter fade. Predictive back is disabled in the manifest (`android:enableOnBackInvokedCallback="false"`) because the system’s edge-swipe preview scaled the page.

### Main data flows

- **Wishlist:** `HomeViewModel` combines active items with ledger summaries, plans, recent events, and collections. `ItemEditorViewModel` saves via `WishlistRepository.saveItem`. Images are copied by `ItemImageStore`; Room stores only a relative path.
- **Savings:** `SavingsRepository.recordDeposit`/`recordWithdrawal` append events inside `database.withTransaction`. Balances are always derived via `SavingsCalculator`. A deposit that crosses the target triggers `GoalNotifier` from the before/after balance, not from a flag.
- **Plans:** `SavingsPlanRepository.savePlan` stores contribution/frequency/reminder fields. Projections are computed by `SavingsPlanCalculator` and never create ledger events.
- **Purchase:** `PurchaseRepository.purchaseItem` runs one transaction: insert purchase record, insert one `PURCHASE_SPEND` for the entire available balance when balance > 0, set item `PURCHASED`. `restoreItem` only flips status back to `ACTIVE`; ledger history remains.
- **Backup:** `BackupManager` exports a ZIP (`manifest.json`, `data.json`, `images/`), validates on restore, stages images under new app-owned names, then replaces all Room tables in one transaction. Restore is a full replacement, not a merge.
- **Notifications:** `ReminderCoordinator` computes the next plan/summary occurrence and asks `ReminderAlarmScheduler` to set an exact alarm when permitted, otherwise a Doze-safe inexact alarm. `ReminderAlarmReceiver` posts and reschedules. `ReminderRescheduleReceiver` restores alarms after boot/app update/permission change.
- **Updates:** `UpdateChecker` fetches the release asset `update.json`; `UpdateManager` throttles automatic checks (24h), applies the 3-day “Later” snooze and per-version skip, and exposes the dialog state. No silent APK installation.
- **Settings/language:** `SettingsRepository` uses Preferences DataStore `settings`. `MainViewModel` exposes settings + onboarding; `MainActivity` applies theme/accent/dynamic color/language. `LanguageStore` is the singleton used by ViewModels/notifications. Update check state is in a separate `update_state` DataStore and is **not** backed up.

### Notification details

- Channels: `plan_reminders` (default importance), `goal_reached` (default), `progress_summaries` (low).
- Notification IDs: plan reminder = `planId.hashCode()`; goal = `100_000 + itemId.hashCode()`; summary = `5001`; test notification = `5002`.
- Plan reminders use `reminderAnchorDate` (DB v2); summary reminders use `AppSettings.summaryAnchorDate`.
- `ReminderAlarmScheduler` uses exact alarms when `canScheduleExactAlarms()` and `setAndAllowWhileIdle` otherwise.
- `ReminderRescheduleReceiver` listens for boot, package replacement, and exact-alarm permission changes.
- Settings → Notifications exposes exact-alarm/battery status, OEM background-activity help, and a test notification.

### UI feedback and accessibility (Track A)

- `ui/components/AppFeedback.kt` defines `AppFeedback` and `LocalAppFeedback`.
- `WhenYahApp` owns `SnackbarHostState`, provides `LocalAppFeedback`, and passes haptics.
- Screens call `feedback.success(...)` / `feedback.error(...)` for transient confirmation, snackbar text, and haptics.
- The item editor and plan editor focus the first invalid field; form errors use `liveRegion`.
- Chip groups that can overflow at large fonts use `FlowRow`.
- Progress bars have percentage content descriptions in addition to visible percentage text.
- `IdrAmountVisualTransformation` is shared by the item editor and plan editor.

---

## D. Database and Backend

### Room database

- File name: `whenyah.db`
- Version: **2** (`WHEN_YAH_DATABASE_VERSION = 2`)
- Schema exports: `app/schemas/id.my.f1zz.whenyah.data.local.WhenYahDatabase/1.json` and `2.json`
- Room builder: `di/DatabaseModule.kt`, with `MIGRATION_1_2`; destructive migrations are not enabled.
- Converters (`data/local/Converters.kt`): UUID ↔ TEXT, Instant ↔ epoch millis INTEGER, LocalDate/LocalTime ↔ ISO TEXT, enums ↔ name TEXT.

### Tables

`collections`

```text
id TEXT PK
name TEXT
createdAt INTEGER (epoch millis)
updatedAt INTEGER (epoch millis)
```

`wishlist_items`

```text
id TEXT PK
name TEXT
targetPriceIdr INTEGER
imageRelativePath TEXT NULL
purchaseUrl TEXT NULL
notes TEXT NULL
category TEXT NULL
collectionId TEXT NULL  → collections.id ON DELETE SET NULL
status TEXT             # ACTIVE / PURCHASED
createdAt INTEGER
updatedAt INTEGER
purchasedAt INTEGER NULL
indexes: collectionId, status
```

`wishlist_items` has **at most one collection** (`collectionId` is a single column, no join table). Deleting a collection leaves its items but sets `collectionId` to NULL via the foreign key.

`savings_events`

```text
id TEXT PK
itemId TEXT             → wishlist_items.id ON DELETE CASCADE
type TEXT               # DEPOSIT / WITHDRAWAL / PURCHASE_SPEND
amountIdr INTEGER       # always positive; direction is implied by type
note TEXT NULL
createdAt INTEGER
purchaseRecordId TEXT NULL
indexes: itemId, purchaseRecordId
```

Ledger is append-only. `purchaseRecordId` is a logical link to `purchase_records.id`; it is indexed but **does not** have a declared foreign key.

`savings_plans`

```text
id TEXT PK
itemId TEXT             → wishlist_items.id ON DELETE CASCADE
contributionIdr INTEGER
frequency TEXT          # DAILY / WEEKLY / MONTHLY
reminderEnabled INTEGER (boolean)
reminderLocalTime TEXT NULL
active INTEGER (boolean)
createdAt INTEGER
updatedAt INTEGER
reminderAnchorDate TEXT NULL   # added in v2
index: itemId UNIQUE
```

`savings_plans.itemId` is unique: at most one plan per item.

`purchase_records`

```text
id TEXT PK
itemId TEXT             → wishlist_items.id ON DELETE CASCADE
completedAt INTEGER
savingsSpentIdr INTEGER
index: itemId
```

### Migration 1 → 2

`data/local/Migrations.kt`:

1. Adds `savings_plans.reminderAnchorDate TEXT`.
2. Backfills it for enabled reminders from `date(updatedAt / 1000, 'unixepoch', 'localtime')`.
3. `WHEN_YAH_DATABASE_VERSION` is bumped to 2.

Rationale: weekly/monthly reminders previously re-anchored to “today” on every app start, causing daily notifications. The anchor now persists. The migration has been run successfully on a real Infinix device; no automated migration test exists.

### Preferences DataStore

- `settings` DataStore (`SettingsRepository`):
  - language, theme mode, accent color, dynamic color
  - notifications enabled, goal notifications enabled
  - summary cadence/time, summary anchor date
  - onboarding completion flag (deliberately outside `AppSettings` and backups)
- `update_state` DataStore (`UpdatePreferences`):
  - update checks enabled
  - last checked timestamp
  - last prompted version code/time
  - skipped version code
  - device-local; **not** included in backups

### Backup format

Constants in `data/backup/BackupModels.kt`:

```text
format = "whenyah-backup"
formatVersion = 1
manifest.json
data.json
images/...
```

- Manifest contains format version, database schema version, app version name, export time, data file path/size/SHA-256, and image entries (relative path, ZIP path, size, SHA-256).
- `BackupValidator` checks references, unique IDs, positive amounts, enums, image paths, reminder fields, settings, and optional fields.
- `BackupManager` rejects zip-slip paths, missing/extra/mismatched images, size caps, checksum failures, and newer database schema versions. **Older schema versions are accepted** (important for restore compatibility).
- Restore stages images, then replaces all Room tables in one transaction. It applies settings including language and re-syncs reminders.
- Size caps in `BackupManager`: manifest 1 MB, data 50 MB, each image 25 MB, total images 300 MB, whole backup 350 MB.
- Backups are unencrypted.

### Backend/API/auth/RLS

There is **no backend, API, authentication, authorization, or RLS** in this repository. The documentation requirement to list endpoints/policies is not applicable. The only network call is `UpdateChecker.fetchManifest()` to the GitHub release asset. There are no environment variables or service credentials in the repo.

---

## E. Implementation Status

### Completed (verified in source)

| Area | Status | Evidence / notes |
|---|---|---|
| Wishlist item CRUD | Completed | `WishlistRepository`, `ItemEditorScreen/ViewModel`, Room `wishlist_items` |
| Local image copy/display | Completed | `ItemImageStore`, `ItemImage`, Coil; image path stored relative |
| Collections | Completed | `CollectionsScreen`, `CollectionDetailScreen`, `WishlistRepository`; delete unassigns via FK |
| Savings ledger | Completed | `SavingsRepository`, `SavingsCalculator`; append-only; deposits/withdrawals in transactions |
| Withdrawal validation | Completed | Rejects withdrawals above available balance inside a Room transaction |
| Goal-reached notification | Completed | `SavingsRepository.recordDeposit` detects before/after crossing; `GoalNotifier` |
| Savings plans/projections | Completed | `SavingsPlanRepository`, `SavingsPlanCalculator`; plans never create ledger events |
| Monthly/weekly/daily recurrence | Completed | `SavingsPlanCalculator`; monthly anchor/clamp tested |
| Plan reminders | Completed | AlarmManager scheduler/coordinator; v2 anchor date; boot/app-update reschedule |
| Summary notifications | Completed | `ReminderCoordinator`, `NotificationScheduleCalculator`; weekly/monthly |
| Dashboard | Completed | `DashboardCalculator`, `HomeViewModel`; active items only |
| Purchase completion | Completed | `PurchaseRepository`; atomic, spend entire available balance, zero-balance record |
| Archive/restore | Completed | `ArchiveScreen`, `ArchivedItemDetailScreen`; restore keeps history, balance stays zero |
| Versioned backup/restore | Completed | `BackupManager`, `BackupValidator`; ZIP + checksums + validation + full replacement |
| Themes/appearance | Completed | `Theme.kt`, `SettingsScreen`; system/light/dark, accent, dynamic color |
| English/Indonesian UI | Completed | `AppStrings` + both implementations; `LanguageStore`; onboarding selector |
| First-launch onboarding | Completed | `OnboardingScreen`, DataStore completion flag outside backups |
| Exact/inexact alarms | Completed | `ReminderAlarmScheduler`; `canScheduleExactAlarms` with Doze-safe fallback |
| Reminder reliability settings | Completed | Exact-alarm and battery-optimization status, OEM help, test notification |
| GitHub update checks | Completed | Release-asset `update.json`, 24h throttle, 3-day Later, skip, manual check |
| DB migration v1 → v2 | Completed | `MIGRATION_1_2`; verified on device |
| Feedback/haptics/snackbars | Completed | `AppFeedback`, `LocalAppFeedback`, shared `SnackbarHostState` |
| Validation focus/live regions | Completed | Item/plan editors; screen-reader error announcements |
| Large-font chip wrapping | Completed | `FlowRow` in Home overview, plan frequency, Settings theme/summary groups |
| IDR input grouping | Completed | Shared `IdrAmountVisualTransformation`; tested `10.000` |
| Update “Skip this version” | Completed | `UpdatePreferences.skippedVersionCode`, policy tests |

### Partially implemented

| Area | Status | Remaining |
|---|---|---|
| Accessibility | Partial | Structural semantics/live regions/progress descriptions added; full TalkBack and contrast audit still needed |
| Large-font support | Partial | Home, Settings, and item editor tested at 200%; other screens still need a pass |
| Update flow | Partial | Unit-tested; real v1.2 release/manifest not published from this working tree |
| Release process | Partial | Local signed v1.1 APK exists; GitHub release status not verifiable from the repo; no CI |
| Home FAB | Partial | Hides while search is focused; can still overlap scrollable content at rest |

### Not yet implemented

- Track B from `docs/ROADMAP_V1.2.md`: filters/sort, collection totals, ready-to-buy section, quick deposit, category suggestions.
- Optional A7 backup-age nudge.
- Real Compose UI/instrumentation tests.
- CI pipeline.
- Encrypted backups (not planned).
- Cloud sync/accounts/bank/payments/multi-currency (explicit non-goals).

### Broken / needs investigation

- No known compile/runtime failure in the current working tree; `assembleDebug`, unit tests, and lint pass.
- The update checker depends on an external release asset; if it is missing or the release is marked prerelease, checks fail silently by design.
- The app is version 1.3 (`versionCode = 4`), but v1.3 is not published yet.
- The test device at handoff time had a fresh debug install with empty app data; that is device state, not repository state.

---

## F. Important Technical Decisions

### 1. Offline-first, no backend

The app has no account system, server, database service, or API. This avoids privacy/operational cost and keeps the product usable without network. The only exception is the opt-out update check.

### 2. IDR-only integer ledger

All money is stored as integer rupiah (`Long`/SQLite `INTEGER`). No `Double`/`Float`, no currency selector, no FX. Formatting is centralized in `domain/format/IdrFormatter.kt` and `ui/util/Formatters.kt`.

### 3. Append-only ledger and derived balances

`savings_events` is the source of truth. `SavingsCalculator.balance`/`summarize` derive the balance and progress. There is no mutable balance column. Withdrawals are validated inside a Room transaction. Progress display caps at 100% while retaining surplus.

### 4. Purchase spends the entire available balance

`PurchaseAccounting.plan(availableBalance)` decides spend amount. `PurchaseRepository` writes one `PURCHASE_SPEND` for the whole balance and a purchase record; zero balance writes only the record (no zero-amount event). Restore only changes status; history is preserved.

### 5. Plans are projections, never transactions

`SavingsPlanCalculator` treats the first contribution as today for estimates. No ledger events are inserted. Monthly recurrence uses the plan anchor day-of-month with end-of-month clamping.

### 6. AlarmManager instead of WorkManager

WorkManager jobs were correctly scheduled but on the test Infinix device they were deferred until the app was opened. Reminders now use AlarmManager:

- exact alarms via `setExactAndAllowWhileIdle` when `canScheduleExactAlarms()`;
- otherwise `setAndAllowWhileIdle` (Doze-safe but inexact);
- boot/app-update/permission-change receivers re-register alarms;
- Settings exposes exact-alarm and battery-optimization status.

### 7. Persistent reminder anchor dates

Weekly/monthly reminders previously used `LocalDate.now()` on every sync, so opening the app before the reminder time caused a daily notification. Plan anchors are now stored on `SavingsPlanEntity.reminderAnchorDate` (v2) and set when the reminder is enabled. Summary anchors live in `AppSettings.summaryAnchorDate` and are set when the cadence changes. Monthly recurrence preserves the anchor day-of-month.

### 8. GitHub release-asset update manifest

`UpdateChecker` fetches:

```text
https://github.com/HerrscherX86/when-ychhh/releases/latest/download/update.json
```

This was chosen over GitHub API/raw files because it is stable for the latest published release, avoids API rate limits, and lets the manifest match the APK release. The manifest uses `versionCode` (not version-name strings). Automatic checks are daily; “Later” snoozes 3 days; “Skip this version” suppresses automatic prompts for that version; manual checks bypass snooze/skip. The dialog opens the release page; the app never installs silently.

### 9. Backup as a versioned full replacement

The ZIP format includes checksums and a manifest. Restore validates everything and replaces all data rather than merging, preventing duplicate ledger events or broken references. Older database schema versions are accepted; newer ones are rejected.

### 10. AGP 9 build setup

AGP 9 has built-in Kotlin support; do not add `org.jetbrains.kotlin.android` or kapt. KSP is used for Room and Hilt. Coil and kotlinx-serialization are pinned for Kotlin 2.2 compatibility.

### 11. Track A interaction/accessibility

A shared `AppFeedback` avoids silent closes, gives consistent snackbar/haptic feedback, focuses the first invalid field, uses live regions for errors, and wraps chip groups with `FlowRow` for large fonts.

### Rejected/deferred alternatives

- Silent APK download/install: needs `REQUEST_INSTALL_PACKAGES`/FileProvider and user permission; not chosen.
- Encrypted backups: deferred; would need password/key UX and restore recovery.
- GitHub API/raw manifest: release-asset JSON chosen instead.
- Forced updates: not implemented.
- Shared-element transitions/large visual rewrite: not in scope.
- R8/minification for release: currently disabled; APK is large (~50 MB).
- Cloud sync/accounts/bank/multi-currency: explicitly out of scope.

---

## G. Current Development State

### Git state at handoff

```text
Branch: main
HEAD:   5d94cda — Update v1.2 2nd half
Remote: origin https://github.com/HerrscherX86/when-ychhh.git
Tags:   none
```

Working tree:

- v1.3 source changes are **uncommitted**.
- `docs/ROADMAP_V1.3.md`, `data/widget/`, `data/navigation/`, `ui/components/Adaptive.kt`, the widget resources, and the two new tests are untracked.
- `docs/update.json` and `app/build.gradle.kts` are bumped to v1.3.
- `.opencode/skills/ui-ux-pro-max/scripts/__pycache__/` is untracked and unrelated; do not commit it.

### Most recent implementation changes (v1.3 — uncommitted)

- Glance widget + snapshot factory + updater + receiver + widget info XML.
- Notification “Add savings” action and launch routing (`AppLauncher`, `LaunchRequestParser`, `MainActivity` `singleTop`, `depositAmount` route arg).
- Navigation rail, `AdaptiveContent`, adaptive grids, grid FAB overload.
- Predictive back enabled.
- Glance 1.2.0 / Material3 Adaptive 1.3.0 dependencies and new unit tests.

### Unfinished work

- v1.3 device/emulator verification: widget lifecycle and refresh, notification action cold/warm start, predictive back, tablet/foldable rail + grids.
- v1.3 release preparation:
  - confirm `versionCode = 4` and `versionName = "1.3"`;
  - build/sign with the same keystore;
  - publish a normal (non-prerelease) GitHub release with the APK and `update.json`;
  - verify v1.2 → v1.3 install and update detection.
- Full TalkBack/contrast audit and Compose instrumentation tests.
- FAB/content overlap polish.
- Optional future work: two-pane tablet list-detail, widget theming.

### Blockers

- No code blocker.
- Release publication requires the external keystore/password. The keystore is not in the repo.
- v1.3 device verification cannot be completed without a phone/tablet or emulator.

### Known bugs / limitations

- Widgets can lag briefly after a write; Glance schedules updates through WorkManager.
- The widget uses the default Material 3 palette, not the app’s custom accent.
- Home FAB can overlap content at rest; focus-hide is a mitigation, not a full fix.
- Update checks silently fail if the release asset is missing, the release is a prerelease, or the repo URL changes.
- OEM battery managers can still delay reminders.
- Release APKs are unsigned by Gradle and must be signed manually.
- `optimization { enable = false }` makes release APKs large and unminified.
- No CI and no real UI tests.
- Full TalkBack/contrast verification is pending.

### Immediate next steps

1. Run the v1.3 manual verification matrix in `docs/ROADMAP_V1.3.md`.
2. Commit the v1.3 working tree.
3. Build/sign and publish v1.3 with `update.json`.
4. Test update detection from a v1.2 install.
5. Decide whether the next release is tablet list-detail, accessibility/tests, or insights.

---

## H. Development Workflow

### Environment setup

- Android Studio with AGP 9.2.1 support, or CLI.
- JDK 21 is used as the Gradle toolchain; the app’s Java target is 11.
- Android SDK Platform 37 (`compileSdk = 37`) and build-tools (the local machine has 36.0.0/36.1.0/37.0.0).
- `local.properties` must point to the local SDK (`sdk.dir=...`); it is machine-specific and ignored.
- Gradle wrapper 9.4.1 is included; no separate install step.

### Build/test commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "id.my.f1zz.whenyah.ExampleUnitTest"
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest   # needs a device/emulator
```

Configuration cache is enabled. If the Gradle daemon crashes from memory pressure, use:

```bash
./gradlew <task> --no-daemon -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8"
```

### Test status

78 host-side tests pass:

```text
BackupValidatorTest 15
DashboardCalculatorTest 7
ExampleUnitTest 1
IdrInputFormattingTest 2
ItemValidationTest 5
NotificationScheduleCalculatorTest 11
PurchaseAccountingTest 5
SavingsCalculatorTest 9
SavingsPlanCalculatorTest 13
UpdateManifestTest 1
UpdatePolicyTest 9
```

Android Lint: **0 errors, 11 warnings** (resource/dependency warnings, not code failures).

### Release workflow

Full details are in `docs/RELEASING.md`. Summary:

1. Bump `versionCode`/`versionName` in `app/build.gradle.kts`.
2. Update `docs/update.json` with the same `versionCode`/`versionName`.
3. Build:

```bash
./gradlew :app:assembleRelease
# output: app/build/outputs/apk/release/app-release-unsigned.apk
```

4. Sign with the **same keystore** used for v1.0/v1.1 (Android Studio “Generate Signed Bundle / APK” is the documented path).
5. Verify version and signature with `aapt2 dump badging` and `apksigner verify --print-certs`.
6. Install over an older signed build on a clean device/emulator to confirm updates work.
7. Create a normal (not prerelease) GitHub release.
8. Attach:
   - signed APK;
   - `update.json` (asset name exactly `update.json`).
9. Verify:

```bash
curl -L https://github.com/HerrscherX86/when-ychhh/releases/latest/download/update.json
```

10. Test the in-app manual update check on the new version.

Local signed artifacts may exist under `app/release/` on the development machine, but `app/release/` is gitignored and must not be committed. The release keystore is external; do not commit it or `keystore.properties`.

### Database schema export

Room exports schemas to `app/schemas/` via KSP. When changing the schema:

1. bump `WHEN_YAH_DATABASE_VERSION`;
2. add an explicit migration in `data/local/Migrations.kt`;
3. add the new schema JSON generated by the build;
4. update backup schema handling/validation;
5. test restore from an older backup and an older DB.

Do not enable destructive migrations.

---

## I. Known Issues and Technical Debt

1. **Unminified release.** `optimization { enable = false }`; the v1.0/v1.1 APKs are ~50 MB. Enabling R8 would reduce size but needs a safe release test.
2. **No CI.** All verification is local/manual.
3. **Only scaffold instrumentation tests.** No real Compose UI tests for editor, backup, notifications, or update dialog.
4. **Home FAB overlap.** The FAB can cover scrollable content at rest. Hiding it while search is focused helps, but a layout audit is still desirable.
5. **Accessibility audit incomplete.** Semantics/live regions/progress descriptions were added, but TalkBack and contrast need a full manual pass.
6. **Update asset dependency.** `update.json` must be attached to every release; otherwise checks fail silently. The release must not be a prerelease for `releases/latest`.
7. **v1.0 update gap.** v1.0 has no checker; users must install v1.1+ manually once.
8. **OEM battery savers.** Exact alarms help, but Infinix/Xiaomi/Oppo/etc. can still delay background work. Settings offers guidance.
9. **Manual release signing.** The keystore is external; losing it breaks future updates for existing users. The same key is required.
10. **Migration coverage.** `MIGRATION_1_2` was verified on-device but has no automated migration test.
11. **`purchaseRecordId` has no FK.** The link from `savings_events` to `purchase_records` is logical, validated by backup rules and repository writes.
12. **Version mismatch risk.** Source version is still 1.1 while Track A is committed; do not publish 1.2 without bumping both the APK and `update.json`.
13. **Roadmap checkboxes are stale.** `docs/ROADMAP_V1.2.md` was written before Track A was implemented, so its Track A checkboxes are unchecked even though the work is committed.
14. **Local docs can lag.** `README.md` is Indonesian and user-facing; the PRD and current source are more authoritative.
15. **No license file.** The README says the license is not set.

---

## J. Agent Handoff Instructions

### Read first

1. `AGENTS.md` — always-loaded working rules.
2. This file — current state, history, gaps.
3. `wishlist_savings_tracker_PRD_v1.2.md` — product decisions/invariants.
4. `docs/ROADMAP_V1.2.md` — Track A/B plan (remember the checkboxes are stale).
5. `docs/RELEASING.md` — signing/release process.
6. `git log --oneline -10` and `git status` before editing anything.

### Avoid changing casually

- The IDR-only integer ledger, derived balances, purchase spend-all logic, plan projection semantics, and one-collection-per-item rule.
- Room schema/version/migrations without explicit migration + backup compatibility work.
- Backup format/validation/restore replacement semantics.
- Reminder anchor semantics, AlarmManager scheduling, notification channels, and exact-alarm fallback.
- Update manifest URL/versionCode contract, 24h/3-day/skip behavior, and release-asset requirement.
- `AppStrings` centralization (add English and Indonesian together).
- Release keystore or any signing secrets.
- The uncommitted `AGENTS.md` update, `docs/agent-handoff.md`, and `docs/ROADMAP_V1.2.md` — do not discard them.

### Inspect first when continuing

- `app/src/main/java/id/my/f1zz/whenyah/data/local/` for schema/migrations.
- `data/repository/`, `domain/`, `data/backup/`, `data/notification/`, `data/update/`.
- `ui/WhenYahApp.kt`, `ui/MainActivity.kt`, `ui/settings/`, `ui/item/` for current UI patterns.
- `app/src/test/` for existing behavior contracts.

### Task to continue

The likely next task is **v1.2 release preparation**, not new feature work:

1. Confirm with the owner whether to release Track A as v1.2 now or continue with Track B.
2. If releasing:
   - bump `versionCode` to 3 and `versionName` to "1.2";
   - update `docs/update.json`;
   - build/sign with the existing keystore;
   - publish a normal GitHub release with the APK and `update.json`;
   - verify v1.1 → v1.2 and the in-app check.
3. If continuing development, implement Track B from `docs/ROADMAP_V1.2.md` and do not bump the release version until the scope is complete.
4. Run the full local verification before handing back:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

### Uncertainty checklist

- Whether the v1.1 GitHub release is published and has the `update.json` asset could **not** be verified from the local repo; there are no local tags.
- The release keystore path, alias, and passwords are external and unknown.
- The local `app/release/` APKs are gitignored and may not match the current HEAD.
- The connected test device had no When Yah install when the Track A device pass began; a fresh debug build was installed with empty data. That device state is not a source of project truth.
- The original pre-test system font scale on the device was not recorded; the setting was later restored to 1.0.
- No backend/API/auth/RLS exists, so those sections are intentionally not applicable.
