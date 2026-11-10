# When Yah — Agent Guide

## Repo state

- Single-module Android app (`:app`). Wishlist, collections, savings, plans, dashboard, purchase completion, archive, settings, themes, notifications, versioned ZIP backup/restore, GitHub update checks, English/Indonesian (Gaul register) UI switching, first-launch onboarding, and the v1.2 Track A feedback/accessibility polish are implemented.
- `wishlist_savings_tracker_PRD_v1.2.md` is the product source of truth. Read PRD §2 (locked decisions), §6 (accounting invariants), §8 (architecture), and §15 (open decisions) before changing behavior. Do not invent behavior for open items; use recommended defaults only when configurable/documented.
- Git repo is initialized on `main`; remote `HerrscherX86/when-ychhh`. `README.md`, the PRD, `docs/RELEASING.md`, `docs/ROADMAP_V1.2.md`, `docs/update.json`, and screenshots exist. There is no CI, pre-commit, license file, or custom lint/format config.
- Current version is **1.1** (`versionCode = 2`). The v1.2 Track A work is committed in `a3d61a5 Update v1.2 1st Half`; the version has not been bumped to 1.2 and `docs/ROADMAP_V1.2.md` is currently untracked. Detailed state: `docs/agent-handoff.md`.
- README/product docs may lag the source; verify behavior in code and the PRD instead of assuming documentation is current.

## Build and toolchain

- Gradle wrapper 9.4.1, AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Java target 11.
- `minSdk = 31` is locked by the PRD; `compileSdk = 37`; `targetSdk = 36`. Package is `id.my.f1zz.whenyah`.
- AGP 9 uses built-in Kotlin: do not apply `org.jetbrains.kotlin.android` or kapt. Use KSP (2.3.6) and Hilt (2.59.2+ required; currently 2.60.1).
- Coil is pinned to 3.3.0 and kotlinx-serialization to 1.9.0 until Kotlin is upgraded. Newer releases pull Kotlin 2.3/2.4 metadata that this project's Kotlin 2.2 compiler cannot read.
- Dependencies/versions live in `gradle/libs.versions.toml`. Add a version plus library/plugin alias there, then reference `libs.*` from `app/build.gradle.kts`.
- `local.properties` (`sdk.dir`) is machine-specific and gitignored.

## Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:testDebugUnitTest --tests "id.my.f1zz.whenyah.ExampleUnitTest"
./gradlew :app:lintDebug
./gradlew :app:connectedDebugAndroidTest   # requires a device/emulator
```

- Host-side unit tests live in `app/src/test`; instrumented tests in `app/src/androidTest`. The latter contains only the scaffold test.
- Gradle's configuration cache is enabled in `gradle.properties`; add `--no-configuration-cache` if a configuration-cache error blocks you.
- Last verified: `:app:assembleDebug`, `:app:testDebugUnitTest` (**78 tests**), and `:app:lintDebug` (**0 errors, 11 warnings**).
- The Gradle daemon has crashed under memory pressure on this machine. If that happens, use:
  `./gradlew <task> --no-daemon -Dorg.gradle.jvmargs="-Xmx3g -Dfile.encoding=UTF-8"`.
- Release build: `./gradlew :app:assembleRelease` produces `app/build/outputs/apk/release/app-release-unsigned.apk`. Sign it with the existing release keystore; follow `docs/RELEASING.md`. Never commit the keystore or passwords.

## Product rules that are easy to violate

- Money is IDR only: store integer rupiah in `Long`/SQLite `INTEGER`. Never use `Double`/`Float`, currency selectors, FX code, or currency network calls. Format as `Rp 1.500.000`.
- Savings is an append-only ledger (`DEPOSIT` +, `WITHDRAWAL` −, `PURCHASE_SPEND` −). Derive balances from events; never keep a second mutable balance. Ordinary withdrawals must not make a balance negative.
- Plans are projections only. Never insert ledger events for planned contributions; "starts today" affects estimates, not actual savings.
- Mark purchased atomically: write one `PURCHASE_SPEND` for the entire available balance, set `PURCHASED`, and capture the purchase record. Zero available balance means a purchase record with no zero-amount event. Restoring keeps history and starts at zero savings.
- Each item has zero or one collection; deleting a collection unassigns its items instead of deleting them.
- Use overflow-safe integer arithmetic for money sums; cap progress display at 100% while retaining surplus savings.

## Architecture

- Wired now: Compose/Material 3 UI in `ui/`, Navigation Compose in `ui/WhenYahApp.kt` + `navigation/`, Hilt in `WhenYahApplication.kt` + `di/`, Room in `data/local/`, and Coil for local image display.
- Wishlist data flows through `data/repository/WishlistRepository.kt`. Images are copied into `filesDir/item_images` by `ItemImageStore`; Room stores only the relative path. Deleting an item deletes its image.
- Savings flows through `data/repository/SavingsRepository.kt`. Balance and progress are always derived with `domain/savings/SavingsCalculator.kt`; never store a mutable balance. Withdrawals are validated and inserted inside a Room transaction so concurrent withdrawals cannot overdraw.
- Plans flow through `data/repository/SavingsPlanRepository.kt`; projections live in `domain/plan/SavingsPlanCalculator.kt`. The first planned contribution is always today, plans never create ledger entries, and monthly recurrence is anchored to the start day-of-month with end-of-month clamping. `observeToday()` refreshes estimates when the local date changes.
- Purchases flow through `data/repository/PurchaseRepository.kt`. Marking purchased atomically writes the purchase record, one `PURCHASE_SPEND` for the entire available balance (omitted when zero), and sets the item `PURCHASED`. Restoring only flips status back to `ACTIVE`; ledger history is preserved and the balance stays zero.
- Dashboard aggregation lives in `domain/dashboard/DashboardCalculator.kt` and consumes active items only. Home combines the summary, recent manual activity, upcoming active plans, nearest goals, ready-to-buy items, and the all-items list. Search and filters run through `domain/wishlist/WishlistQuery.kt` (`WishlistQueryEngine`): text search, collection, category, goal status, and plan presence, plus recent/name/progress/price sorting. Filters apply to the all-items list only and are held in memory, so they reset on process death.
- Collection totals reuse `DashboardCalculator.summarize` per collection (grouped from `observeActiveItemsWithSavings`), so collection cards and the collection detail header show target, saved, and progress without a second calculator.
- Settings persist through `data/settings/SettingsRepository.kt` (Preferences DataStore named `settings`). `MainViewModel` applies theme mode, accent color, dynamic color, and language in `MainActivity`; `SettingsViewModel` owns settings updates and reminder rescheduling.
- First-launch onboarding lives in `ui/onboarding/OnboardingScreen.kt` (three-page pager, language menu, skip/next/start). The completion flag is stored in the same DataStore but intentionally outside `AppSettings`/backups, so reinstalling shows onboarding once and restoring a backup never resets it. `MainActivity` waits for the flag before showing `WhenYahApp`.
- UI copy is centralized in `domain/i18n/`. `AppStrings` has `EnglishStrings` and `IndonesianStrings` (friendly Gaul register, `aku/kamu`, common slang, technical terms kept in English). Compose reads `LocalAppStrings`; ViewModels/notifications use the singleton `LanguageStore`. When adding user-facing text, add it to the interface and both implementations, use `strings.locale` for date formatting, and never leave hardcoded UI copy.
- Transient UI feedback runs through `ui/components/AppFeedback.kt`: `WhenYahApp` owns the `SnackbarHostState` and provides `LocalAppFeedback`, and screens call `success`/`error` for a snackbar plus haptics instead of silently closing actions. Form validation focuses the first invalid field; error text uses `liveRegion`; large-font chip groups use `FlowRow`; progress bars expose a percentage content description alongside the visible percentage text.
- Notifications use `data/notification/`: `NotificationService` creates channels/posts, `ReminderCoordinator` computes next occurrences and schedules them through `ReminderAlarmScheduler` (AlarmManager exact alarms when permitted, Doze-safe inexact fallback otherwise), and `ReminderAlarmReceiver` obtains the coordinator through `ReminderEntryPoint`. `ReminderRescheduleReceiver` re-registers alarms after boot, app updates, and exact-alarm permission changes. Goal-reached notifications are detected inside `SavingsRepository.recordDeposit` from the actual before/after balance crossing, so no separate “notified” flag is needed. Settings → Notifications shows exact-alarm and battery-optimization status, OEM background-activity help, and a test-notification action that uses the plan-reminder channel.
- Update checks live in `data/update/`: `UpdateChecker` fetches the `update.json` asset from `https://github.com/HerrscherX86/when-ychhh/releases/latest/download/update.json`, `UpdateManager` runs automatic checks at most once per 24 hours and snoozes the dialog per version for 3 days, and `UpdatePreferences` keeps check state device-local and outside backups. Automatic checks are on by default, can be disabled in Settings → About, and manual checks bypass the snooze. The dialog also supports “Skip this version” (`skippedVersionCode` in `UpdatePreferences`); skipping suppresses automatic prompts for that version while manual checks still show it. The dialog opens the GitHub release page; the app never installs an APK silently.
- There is no backend, API server, authentication, authorization, or RLS. The app is local-first; the only network request is the GitHub update manifest. Do not add a backend/account system unless the product owner explicitly requests it.
- The update manifest is the **release asset** `update.json` under `releases/latest/download/`. It must be attached to every non-prerelease GitHub release or update checks fail silently. `UpdateChecker` hardcodes the repo URL; renaming the repository requires updating it.
- Release builds are currently unminified (`optimization { enable = false }`), and `assembleRelease` is unsigned. Android updates require the same signing key and a higher `versionCode` than the installed app.
- Plan reminders are stored on `SavingsPlanEntity` (`reminderEnabled`, `reminderLocalTime`, `reminderAnchorDate`) and scheduled/rescheduled from the plan editor, settings changes, purchase/restore, and app start. `reminderAnchorDate` is set when the reminder is enabled and preserved across plan edits, so weekly/monthly schedules do not drift to “today” on every app start; monthly recurrence keeps that anchor day-of-month with end-of-month clamping. Purchased items must not keep reminder jobs.
- Backup/restore lives in `data/backup/`. Export writes a versioned ZIP with `manifest.json`, `data.json`, and `images/`; the manifest records format/schema versions, sizes, and SHA-256 checksums. Restore validates manifest/schema/checksums/relations/sizes, stages images under new app-owned names, then replaces all Room tables in one transaction and applies settings including language. It cancels and re-syncs reminders after restore; it must never replay transactions or fire retroactive notifications.
- `WhenYahDatabase` is version 2 with the PRD's core entities; v2 adds `SavingsPlanEntity.reminderAnchorDate`. Room schemas export to `app/schemas/`; bump the version in `DatabaseConstants.kt`, add explicit migrations (`MIGRATION_1_2` backfills the anchor from `updatedAt`), and keep backup schema handling in sync. Backups with older database schema versions must still restore; only newer versions are rejected. Do not silently enable destructive migrations.
- Add entities under `data/local/entity`, DAOs under `data/local/dao`, Hilt providers in `di/DatabaseModule.kt`, and screen ViewModels next to their screens.
- Keep balance, projection, purchase, aggregation, backup-validation, and reminder-schedule logic out of Compose/Android UI classes so it stays unit-testable. `ItemValidationTest` covers IDR parsing, names, and purchase URL validation; `SavingsCalculatorTest` covers ledger balances, progress, surplus, zero targets, withdrawal limits, and overflow; `SavingsPlanCalculatorTest` covers the PRD acceptance table, rounding, daily/weekly recurrence, monthly anchor/clamp, leap years, year boundaries, and far-future projections; `DashboardCalculatorTest` covers totals, funded counts, zero-target cases, and overflow; `PurchaseAccountingTest` covers spend-all, zero-balance, history preservation, and overfunded purchases; `NotificationScheduleCalculatorTest` covers today/passed-time reminders, weekly/monthly recurrence, leap-day clamping, and summary schedules; `BackupValidatorTest` covers JSON round-trip, references, amounts, enums, image paths, reminders, settings, language restoration, and older backups without the plan anchor. `UpdatePolicyTest` covers version comparison, 3-day snooze, skip, and manual overrides; `UpdateManifestTest` covers manifest JSON; `IdrInputFormattingTest` covers IDR input grouping; `WishlistQueryTest` covers text/collection/category/goal/plan filtering, the four sort orders, stable tie-breaks, and combined filters.
- `ui/onboarding` is the first-launch three-page pager with a language selector and skip/next/start actions. `ui/home` is the dashboard plus the all-items list: a primary-container overview hero, a compact three-row activity card, actionable plan cards, horizontal closest-goals and ready-to-buy carousels, then searchable and filterable items (filter bottom sheet, removable applied-filter chips). `ui/home/ActivityScreen` is the secondary “View all” activity route. Fully funded items are filtered from Home's upcoming plans for display only; savings/reminder business logic is unchanged. `ui/collections` has collection list/detail with per-collection target/saved/progress totals; `ui/item` has active item detail (with a quick-deposit button that only pre-fills the transaction dialog from the plan contribution), an expressive add/edit editor (single image-upload surface, filled rounded fields, IDR input grouping, collection bottom sheet, category suggestion chips backed by existing items, collapsible optional details, anchored save bar, dirty-form guard), transaction dialog/history, and plan editor with reminder settings; `ui/archive` has purchased list/read-only detail; `ui/settings` has appearance, language, notification, data (archive + backup/restore), and about sections. `LocalSetTopBarBackHandler` lets a screen guard the shared top-bar back action. `ExampleUnitTest` and `ExampleInstrumentedTest` are scaffold tests to replace.
- Home's `LazyColumn` intentionally shows the same item in multiple sections (nearest goals, ready to buy, and all items). LazyColumn keys must be globally unique, so prefix section keys (`item-`, `nearest-`, `ready-`, `activity-`, `plan-`). Reusing a raw item UUID across sections crashes when scrolled.

## Other product constraints

- The app is offline-first. The only network access is the update check against the GitHub release asset; it is opt-out in Settings and must fail silently. Notifications are opt-in; scheduled reminders use `AlarmManager` (exact via `SCHEDULE_EXACT_ALARM` when granted, `setAndAllowWhileIdle` otherwise), so aggressive OEM battery savers can still delay them. Request `POST_NOTIFICATIONS` on Android 13+ and keep all features working when permission is denied. Weekly/monthly schedules use local time, anchor to the day the reminder or summary was enabled, and monthly recurrence keeps that day-of-month with end-of-month clamping.
- Copy picked image bytes into app-owned private storage. Backups/restores use SAF and must validate archives, reject zip-slip paths, cap entry sizes, and avoid partial imports. Restoring is a full replacement, not a merge.
- Never commit the release keystore, `keystore.properties`, `local.properties`, signed APKs/AABs, or `app/release/`. These are gitignored for a reason.
- No Node/npm project exists here; use Gradle only. There is no CI pipeline, so run the local build/test/lint commands before handing work back.

## Current gaps and caution

- v1.2 Track A is committed but the app is still version 1.1. At release time, bump to `versionCode = 3` / `versionName = "1.2"`, update `docs/update.json`, sign with the same keystore, and publish `update.json` as a release asset.
- v1.0 users have no update checker; they need to install v1.1 (or later) manually once.
- The Home FAB can still overlap scrollable content at rest; hiding it while the search field is focused is the current mitigation.
- Full TalkBack/contrast verification and Compose instrumentation tests are still pending.
- Aggressive OEM battery savers can delay reminders even with exact alarms; Settings shows reliability guidance.

## Docs map

- `docs/agent-handoff.md` — detailed state, history, feature status, known issues, and next steps.
- `docs/RELEASING.md` — signing and GitHub release checklist.
- `docs/ROADMAP_V1.2.md` — Track A (committed) and Track B (planned) scope.
- `wishlist_savings_tracker_PRD_v1.2.md` — product source of truth.
