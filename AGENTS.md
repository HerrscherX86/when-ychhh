# When Yah — Agent Guide

## Repo state

- Single-module Android app (`:app`). Wishlist, collections, savings, plans, dashboard, purchase completion, archive, settings, themes, notifications, versioned ZIP backup/restore, English/Indonesian (Gaul register) UI language switching, and first-launch onboarding are implemented.
- `wishlist_savings_tracker_PRD_v1.2.md` is the product source of truth. Read PRD §2 (locked decisions), §6 (accounting invariants), §8 (architecture), and §15 (open decisions) before changing behavior. Do not invent behavior for open items; use recommended defaults only when configurable/documented.
- Git repo is initialized. No README, CI, pre-commit, or custom lint/format config exists.

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

- Host-side unit tests live in `app/src/test`; instrumented tests in `app/src/androidTest`.
- Gradle's configuration cache is enabled in `gradle.properties`; add `--no-configuration-cache` if a configuration-cache error blocks you.
- Full verification currently passes: `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug`.

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
- Dashboard aggregation lives in `domain/dashboard/DashboardCalculator.kt` and consumes active items only. Home combines the summary, recent manual activity, upcoming active plans, nearest goals, and the all-items list.
- Settings persist through `data/settings/SettingsRepository.kt` (Preferences DataStore named `settings`). `MainViewModel` applies theme mode, accent color, dynamic color, and language in `MainActivity`; `SettingsViewModel` owns settings updates and reminder rescheduling.
- First-launch onboarding lives in `ui/onboarding/OnboardingScreen.kt` (three-page pager, language menu, skip/next/start). The completion flag is stored in the same DataStore but intentionally outside `AppSettings`/backups, so reinstalling shows onboarding once and restoring a backup never resets it. `MainActivity` waits for the flag before showing `WhenYahApp`.
- UI copy is centralized in `domain/i18n/`. `AppStrings` has `EnglishStrings` and `IndonesianStrings` (friendly Gaul register, `aku/kamu`, common slang, technical terms kept in English). Compose reads `LocalAppStrings`; ViewModels/notifications use the singleton `LanguageStore`. When adding user-facing text, add it to the interface and both implementations, use `strings.locale` for date formatting, and never leave hardcoded UI copy.
- Notifications use `data/notification/`: `NotificationService` creates channels/posts, `ReminderCoordinator` schedules WorkManager one-time jobs and computes next occurrences, and `PlanReminderWorker`/`SummaryWorker` obtain the coordinator through `ReminderWorkerEntryPoint`. Goal-reached notifications are detected inside `SavingsRepository.recordDeposit` from the actual before/after balance crossing, so no separate “notified” flag is needed.
- Plan reminders are stored on `SavingsPlanEntity` (`reminderEnabled`, `reminderLocalTime`) and scheduled/rescheduled from the plan editor, settings changes, purchase/restore, and app start. Purchased items must not keep reminder jobs.
- Backup/restore lives in `data/backup/`. Export writes a versioned ZIP with `manifest.json`, `data.json`, and `images/`; the manifest records format/schema versions, sizes, and SHA-256 checksums. Restore validates manifest/schema/checksums/relations/sizes, stages images under new app-owned names, then replaces all Room tables in one transaction and applies settings including language. It cancels and re-syncs reminders after restore; it must never replay transactions or fire retroactive notifications.
- `WhenYahDatabase` is version 1 with the PRD's core entities. Room schemas export to `app/schemas/`; bump the version in `DatabaseConstants.kt`, add explicit migrations, and keep backup schema handling in sync. Do not silently enable destructive migrations.
- Add entities under `data/local/entity`, DAOs under `data/local/dao`, Hilt providers in `di/DatabaseModule.kt`, and screen ViewModels next to their screens.
- Keep balance, projection, purchase, aggregation, backup-validation, and reminder-schedule logic out of Compose/Android UI classes so it stays unit-testable. `ItemValidationTest` covers IDR parsing, names, and purchase URL validation; `SavingsCalculatorTest` covers ledger balances, progress, surplus, zero targets, withdrawal limits, and overflow; `SavingsPlanCalculatorTest` covers the PRD acceptance table, rounding, daily/weekly recurrence, monthly anchor/clamp, leap years, year boundaries, and far-future projections; `DashboardCalculatorTest` covers totals, funded counts, zero-target cases, and overflow; `PurchaseAccountingTest` covers spend-all, zero-balance, history preservation, and overfunded purchases; `NotificationScheduleCalculatorTest` covers today/passed-time reminders, weekly/monthly recurrence, leap-day clamping, and summary schedules; `BackupValidatorTest` covers JSON round-trip, references, amounts, enums, image paths, reminders, settings, and language restoration.
- `ui/onboarding` is the first-launch three-page pager with a language selector and skip/next/start actions. `ui/home` is the dashboard plus the all-items list: a primary-container overview hero, a compact three-row activity card, actionable plan cards, a horizontal closest-goals carousel, then searchable items. `ui/home/ActivityScreen` is the secondary “View all” activity route. Fully funded items are filtered from Home's upcoming plans for display only; savings/reminder business logic is unchanged. `ui/collections` has collection list/detail; `ui/item` has active item detail, an expressive add/edit editor (single image-upload surface, filled rounded fields, IDR input grouping, collection bottom sheet, collapsible optional details, anchored save bar, dirty-form guard), transaction dialog/history, and plan editor with reminder settings; `ui/archive` has purchased list/read-only detail; `ui/settings` has appearance, language, notification, data (archive + backup/restore), and about sections. `LocalSetTopBarBackHandler` lets a screen guard the shared top-bar back action. `ExampleUnitTest` and `ExampleInstrumentedTest` are scaffold tests to replace.
- Home's `LazyColumn` intentionally shows the same item in multiple sections (nearest goals and all items). LazyColumn keys must be globally unique, so prefix section keys (`item-`, `nearest-`, `activity-`, `plan-`). Reusing a raw item UUID across sections crashes when scrolled.

## Other product constraints

- The app is offline-first. Notifications are opt-in and best-effort (`WorkManager` timing is not exact); request `POST_NOTIFICATIONS` on Android 13+ and keep all features working when permission is denied. Weekly/monthly schedules use local time and monthly recurrence keeps the original day-of-month with end-of-month clamping.
- Copy picked image bytes into app-owned private storage. Backups/restores use SAF and must validate archives, reject zip-slip paths, cap entry sizes, and avoid partial imports. Restoring is a full replacement, not a merge.
