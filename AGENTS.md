# When Yah — Agent Guide

## Repo state

- Single-module Android app (`:app`). Wishlist, collections, savings, plans, dashboard, purchase completion, and the purchased archive are implemented: item CRUD, local images, categories, collection assignment, search/sort, deposits, withdrawals, transaction history, per-item balances, completion progress, daily/weekly/monthly plans, dashboard summaries, purchase-spend accounting, restore, and archive deletion. Notifications and backup are not implemented yet.
- `wishlist_savings_tracker_PRD_v1.2.md` is the product source of truth. Read PRD §2 (locked decisions), §6 (accounting invariants), §8 (architecture), and §15 (open decisions) before changing behavior. Do not invent behavior for open items; use recommended defaults only when configurable/documented.
- The repository is initialized but has no commits yet. No README, CI, pre-commit, or custom lint/format config exists.

## Build and toolchain

- Gradle wrapper 9.4.1, AGP 9.2.1, Kotlin 2.2.10, Compose BOM 2026.02.01, Java target 11.
- `minSdk = 31` is locked by the PRD; `compileSdk = 37`; `targetSdk = 36`. Package is `id.my.f1zz.whenyah`.
- AGP 9 uses built-in Kotlin: do not apply `org.jetbrains.kotlin.android` or kapt. Use KSP (2.3.6) and Hilt (2.59.2+ required; currently 2.60.1).
- Coil is pinned to 3.3.0 until Kotlin is upgraded. Coil 3.4+ pulls Kotlin 2.3/2.4 metadata that this project's Kotlin 2.2 compiler cannot read.
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
- `WhenYahDatabase` is version 1 with the PRD's core entities. Room schemas export to `app/schemas/`; bump the database version and add explicit migrations when changing entities. Do not silently enable destructive migrations.
- Add entities under `data/local/entity`, DAOs under `data/local/dao`, Hilt providers in `di/DatabaseModule.kt`, and screen ViewModels next to their screens.
- Not wired yet: DataStore, WorkManager.
- Keep balance, projection, purchase, and aggregation logic out of Compose/Android UI classes so it stays unit-testable. `ItemValidationTest` covers IDR parsing, names, and purchase URL validation; `SavingsCalculatorTest` covers ledger balances, progress, surplus, zero targets, withdrawal limits, and overflow; `SavingsPlanCalculatorTest` covers the PRD acceptance table, rounding, daily/weekly recurrence, monthly anchor/clamp, leap years, year boundaries, and far-future projections; `DashboardCalculatorTest` covers totals, funded counts, zero-target cases, and overflow; `PurchaseAccountingTest` covers spend-all, zero-balance, history preservation, and overfunded purchases.
- `ui/home` is the dashboard plus the all-items list with search/sort; `ui/collections` has collection list/detail screens; `ui/item` has active item detail, add/edit, transaction dialog/history, and savings-plan editor screens; `ui/archive` has the purchased list and read-only archived detail with restore/delete. `ui/settings` is still a placeholder. `ExampleUnitTest` and `ExampleInstrumentedTest` are scaffold tests to replace.
- Home's `LazyColumn` intentionally shows the same item in multiple sections (nearest goals and all items). LazyColumn keys must be globally unique, so prefix section keys (`item-`, `nearest-`, `activity-`, `plan-`). Reusing a raw item UUID across sections crashes when scrolled.

## Other product constraints

- The app is offline-first. Notifications are opt-in and best-effort (`WorkManager` timing is not exact); request `POST_NOTIFICATIONS` on Android 13+.
- Copy picked image bytes into app-owned private storage. Backups/restores use SAF and must validate archives, reject zip-slip paths, and avoid partial imports.
