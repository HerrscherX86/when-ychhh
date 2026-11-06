# Wishlist & Savings Tracker — Product Requirements Document

**Version:** 1.2 (IDR-only; purchase accounting and Android minimum finalized)  
**Last updated:** 2026-11-06  
**Platform:** Native Android  
**Primary audience:** Personal use first, potential Google Play publication later  
**Status:** Product requirements baseline, not an authorization to invent unspecified product behavior

> **Instructions to coding agent:** Treat requirements tagged **MUST / P0** as MVP acceptance requirements. Respect the decisions in §2. Do not silently invent behavior for items in §15 (Open decisions / blockers). Implement offline-first, deterministic financial arithmetic, and test business logic independently of UI. Where a decision is **Recommended default**, it can be used in an initial implementation but should be configurable or documented and confirmed before releasing.

## 1. Product summary

An offline-first Android application for organizing physical products, digital products (including games and software), books, manga, and light novels that a user wants to buy. Users manually add items, organize them in custom collections, record money set aside for each item, create daily/weekly/monthly fixed-contribution plans, estimate when a goal will be reached, and view aggregate progress. Users can archive purchased items and track spending of the earmarked savings. The app uses Indonesian Rupiah (IDR) exclusively, with local backups, configurable themes, and optional notifications. It does **not** hold, transfer, or verify actual funds.

### Goals

- Make adding, finding, and organizing wanted items easy.
- Make each item's **recorded** savings, target, and remaining amount clear.
- Give understandable estimated completion dates without pretending that planned contributions have already occurred.
- Make every core workflow usable offline.
- Avoid losing money history or images during backup and restore.
- Show all monetary values consistently in Indonesian Rupiah (IDR), without exchange-rate dependencies.

### Non-goals (MVP)

No account, bank integration, payments, shared wallets, product price scraping, automatic price alerts, cross-device sync, wishlist sharing, or actual purchase checkout.

## 2. Locked product decisions

| Area | Final decision | Consequence |
|---|---|---|
| Item entry | Manual only | User types item details and optionally attaches local image/URL. |
| Supported items | Physical products, digital goods, books/manga/light novels | Generic item model and optional category. |
| Fields | Name, price, image, purchase URL, notes, date added, category | Collection membership, status, and IDs are additional system fields. |
| Organization | User-created collections | **Each item belongs to at most one collection** (zero allowed). |
| Savings model | Per-item | Savings for one item are not automatically available to another. |
| Transactions | Manually recorded deposits and withdrawals | Plans NEVER create real savings transactions automatically. |
| Excess savings | **Retain** | Recorded savings may exceed target; UI progress bar caps at 100%, actual balance is not capped. |
| Purchased item | **Spend the entire available recorded savings balance and retain history** | Purchase completion creates a `PURCHASE_SPEND` ledger event for the complete available balance; see FR-04 and §6. |
| Plan cadence | Fixed contributions daily, weekly, monthly | Users choose amount and cadence. |
| Plan start | **Today** | The projection assumes the first planned contribution falls on the local current date, without crediting it to actual savings. |
| Projection | Estimate time to reach target | Use calendar-based recurrence, not a 30-day approximation for monthly. |
| Dashboard | Required | Display goal and recent activity summaries. |
| Currency | **IDR only** | All prices, deposits, withdrawals, savings plans, purchase-spend amounts, and totals are in IDR. No conversion or FX integration. |
| Appearance | Material 3 plus custom styling | System/light/dark and configurable accent/theme. |
| Navigation | Compact | Home, Collections, Settings as bottom destinations. |
| Storage | Offline-first local | No sign-in or backend required. |
| Backups | Manual export/import | Portable versioned package including images. |
| Notifications | Contribution reminders, reached-goal, periodic summaries | Opt-in and permission-aware. |
| Development | Native Android | Kotlin + Jetpack Compose; **minimum Android 12 (API 31)**. |

## 3. User stories and core journeys

1. **Create item:** From Home or Collections, tap Add, enter name, target price in IDR, optional details, then save. Open item detail.
2. **Organize:** Create/rename/delete collections; assign an item to one or no collection; browse items within each collection.
3. **Fund item:** From item detail, record a deposit or withdrawal with positive amount and optional note. Balance and progress update immediately.
4. **Plan savings:** Specify contribution amount and daily/weekly/monthly frequency. See a projected number of contributions and projected goal date assuming the first contribution is **today**. Planned amounts do not modify balance.
5. **Review progress:** Home displays aggregate goals, savings, recently updated items, close-to-goal items, and upcoming plans/reminders.
6. **Mark purchased:** Confirm purchase; app records a purchase-spend event, archives the item, and retains its full audit trail. Archived records leave active dashboard totals.
7. **Backup/restore:** Export a portable backup to user-chosen location; restore from a validated file only after confirmation.
8. **Theme:** Choose an appearance and accent color; persist settings across restarts. All amounts use IDR.

## 4. MVP requirements and acceptance criteria

### FR-01 — Items (P0)

- Create, view, edit, delete active items offline.
- **Required:** name (non-blank), target price in IDR (nonnegative integer rupiah amount). **Optional:** image, purchase URL, notes, category, one collection.
- Automatically record created/updated timestamps. Date added is system-generated.
- Display image/name/IDR target price/savings progress in list and detail.
- Image selected through Android photo picker or Storage Access Framework; **copy needed bytes into app-controlled private storage** so it remains valid if external URI access is revoked. Keep thumbnail performance reasonable.
- Purchase URL is manually supplied and must be validated before opening in an external browser.
- Price edits keep existing recorded savings in IDR and recalculate progress and projected date.
- Confirm destructive deletion; deleting an item removes dependent ledger/plan/image data and cannot be undone without backup (recommend offering Undo or confirmation).
- An item can exist without a collection.

**Acceptance:** Item CRUD and associated image survive process death and restart offline; invalid input does not create a record.

### FR-02 — Collections and categories (P0)

- User may create, rename, remove collections and browse their items.
- Item has **zero or one** collection ID; a collection may contain many items.
- Removing a collection **unassigns items**, never deletes them.
- Category is optional per item (for example, Electronics, Games, Books); category and collection are separate concepts.
- All Items screen (reachable from Home) supports browsing and simple search; recommended sorting by recently added and name.

**Acceptance:** Cannot assign two collection IDs to one item; removal of a collection leaves all its items accessible.

### FR-03 — Manual per-item savings ledger (P0)

- User can add deposits and withdrawals for active items; each is timestamped and optionally annotated.
- Use an **append-only auditable ledger** where possible. Any correction should be explicit (reversal/correction event) or carefully audited, rather than silently editing history.
- Monetary values are stored as nonnegative integer IDR amounts (`Long`), **never binary floating point**. Validate arithmetic overflow and input bounds.
- Positive transaction amounts only. Normal withdrawal may not exceed the currently available recorded savings.
- Available balance is derived from ledger events; never maintained as an unsynchronized second source of truth.
- Allow savings above target with no limit (unless storage bounds). Progress gauge = `min(availableBalance / target, 1)`; show surplus separately.
- If target is zero: mark goal reached; use a clear 100%/no-savings-needed state, not division by zero.
- All ledger events and savings plans are denominated in IDR; there is no currency selection or conversion.

**Acceptance:** For any sequence of valid events, derived balance equals deposits minus withdrawals minus purchase-spend events; no balance becomes negative due to ordinary withdrawals.

### FR-04 — Purchase completion and spend accounting (P0)

**Final decision:** Marking an item purchased records savings as **spent on that item** and preserves the full transaction history.

**Locked deterministic MVP behavior:**

1. Tap **Mark Purchased** on an active item.
2. Show confirmation with item name, current target price, and current earmarked savings.
3. In **one atomic database transaction**, record a `PURCHASE_SPEND` ledger event with amount equal to the available saved balance at purchase time, set item status `PURCHASED`, and capture purchase timestamp and purchase record metadata.
4. The available savings balance becomes zero; the archived detail page shows **saved before purchase**, **spent from savings**, and chronological ledger entries. This is a *virtual accounting entry*, not a bank transaction.
5. Consume **100% of the item's available recorded savings**, whether that balance is less than, equal to, or greater than the item target price. Do **not** fabricate deposits for any shortfall. The app does not track actual checkout price in MVP.
6. Purchased items are excluded from active wishlist values, active savings totals, reminders, and active plan estimates.
7. When an archived item is restored to active, preserve the purchase event and historical records. The restored item starts with zero available savings, and new deposits fund its next goal cycle.

**Important distinction:** *Savings spent* is the amount the user set aside, **not necessarily the full purchase price**. Display those labels separately.

**Acceptance:** Completing a purchase never double-counts spent savings in active totals; archived audit history is intact; atomic failure leaves both status and ledger unchanged.

### FR-05 — Fixed-contribution plans and estimates (P0)

Each active item may have zero or one active plan:

- Contribution amount > 0 in IDR.
- Frequency: `DAILY`, `WEEKLY`, `MONTHLY`.
- Start assumption: **today in device-local calendar** for projections.
- Contributions are projected only, never automatically inserted into the ledger.
- Remaining = `max(targetPrice - availableBalance, 0)`.
- Number of planned contributions = `ceil(remaining / contributionAmount)`.
- If remaining == 0, show **Goal reached** and completion date today.
- If amount <= 0, do not save plan; if no plan, show no estimate.
- For N required contributions, the dates are: today's local date, followed by N−1 frequency occurrences (one per day / 7 days / calendar month). **Estimated goal date = date of the Nth occurrence**. E.g. if one deposit suffices, the estimate is **today**, not tomorrow.
- Monthly recurrence uses a calendar policy for invalid day-of-month (e.g., Jan 31 → Feb 28/29) rather than fixed 30-day increments. Recommended default: anchor to original start-day and clamp to each destination month's last day. Weekly recurs every seven days from today.
- When current date advances, plans recompute their projection starting **today** using only actual recorded savings; the app does not claim missed planned contributions were made.
- Display an explicit qualifier: "Estimate assumes contributions beginning today and continuing on schedule."
- Completion estimates do not model future changes to item price or available income.
- Users can edit, pause, resume, or delete a plan.

**Acceptance tests:**

| Scenario | Target | Saved | Contribution | Cadence | Expected |
|---|---:|---:|---:|---|---|
| Already funded | 1,000 | 1,100 | 100 | weekly | Reached, today, 0 required |
| One contribution | 1,000 | 900 | 100 | weekly | 1 required, today |
| Three contributions | 1,000 | 500 | 200 | weekly | 3 required, today + 14 days |
| Zero goal | 0 | 0 | 100 | daily | Reached, no divide-by-zero |
| Monthly Jan 31 | 300 | 0 | 100 | monthly | 3rd contribution occurs on March 31 with anchor-and-clamp policy |

### FR-06 — Dashboard (P0)

Home displays:

- Number of **active** items.
- Total active target value in IDR.
- Total **available** earmarked savings across active items, in IDR.
- Overall progress as `min(totalAvailableSavings / totalTargetValue, 1)` with a clear zero-total case.
- Count of fully funded active items.
- Recent manual ledger activity.
- Items nearest to their goal.
- Upcoming scheduled contributions or reminders (planned, not paid).
- Entry points to All Items, Add Item, and item details.

All summaries use integer IDR arithmetic, with overflow-safe accumulation. Do not fetch exchange rates or provide a currency selector. Format amounts for Indonesian Rupiah (for example `Rp 1.500.000`).

### FR-07 — IDR-only monetary policy (P0)

- The **sole supported currency is Indonesian Rupiah (IDR)**. Every target price, transaction, savings-plan contribution, purchase record, and aggregate is in IDR.
- Store all amounts as integer rupiah units using Kotlin `Long` / SQLite `INTEGER`. Do not use floating-point arithmetic for money.
- Amounts entered in any other currency are not supported, and the app shall not expose per-item currency or display-currency settings.
- No automatic exchange-rate fetching, conversion, FX cache, provider API, or currency-related network permission is required.
- Prices can be edited in IDR without altering recorded savings; progress and estimates then recalculate.
- Validate nonnegative item prices, positive transaction/contribution amounts, and integer overflow before persisting or totaling values.
- Format with the `IDR` monetary convention consistently, with no fractional rupiah entry in MVP.

**Acceptance:** All totals and transaction histories are accurate IDR values offline. The app makes no network requests for currency data.

### FR-08 — Purchased archive (P0)

- Browse purchased items and open read-only history including purchase-spend event.
- Archive items absent from active totals and contribution reminders.
- Support restoring an item to active with preserved historical transactions and no automatic recreation of spent balance.
- Hard deletion of archived items requires confirmation.

### FR-09 — Notifications (P0)

- **Contribution reminders:** Optional per-plan reminder; schedule by daily/weekly/monthly cadence and selected local time. For "today" plan, initial reminder today only if the configured time has not passed; otherwise the next occurrence. Never automatically deposit money.
- **Goal reached:** Post at most one notification per below-to-at/above-target transition caused by a new actual transaction (not a speculative plan). Reset eligibility if balance falls below target again.
- **Periodic progress summaries:** User-configurable weekly/monthly timing, off by default unless enabled.
- Request `POST_NOTIFICATIONS` permission when appropriate on Android 13+ and gracefully handle denial.
- WorkManager suits deferrable periodic jobs; **exact notification timing cannot be guaranteed** with it. Do not promise exact delivery, and verify any use of alarms against current Android restrictions.
- Reschedule appropriately after app/device restart and preference changes (subject to OS constraints).
- Purchased items never generate future contribution reminders.

### FR-10 — Appearance (P0)

- Compose Material 3 foundation with customized spacing, typography, components, and cards.
- Support system, light, and dark mode, configurable accent color, and optional Material You dynamic colors on supported Android.
- Preferences persist across app restarts and are included in backup where appropriate.
- Meet accessible text scaling/contrast/touch target and screen reader expectations.

### FR-11 — Local backup/restore (P0)

- Export versioned `.zip` package containing JSON metadata/records plus copies of image files and a manifest with schema version and checksums (recommended).
- Use Android Storage Access Framework for user-selected file destination/source; no broad storage permission.
- Include collections, items, ledger, plans, purchase records, and relevant preferences.
- Validate manifest, version, file types, item/collection references, amounts, image checksums, and reasonable maximum file sizes before restoring.
- **Recommended default:** restore replaces local records after an explicit warning and confirmation; perform staged import and transactionally switch state, ensuring failure does not leave partial data. Never blindly unzip paths (zip-slip protection).
- Restore should not trigger retroactive goal notifications or duplicate reminders.
- Explain that uninstalling or losing the phone may lose data if no backup exists.

## 5. Navigation and screen specification

**Bottom destinations (compact):**

1. **Home** — progress dashboard in IDR, recent activity, view all items, quick Add.
2. **Collections** — collection list, create/edit/delete, collection detail and its items.
3. **Settings** — theme, notification preferences, backup/restore, About.

**Secondary routes:** All Items (search), Item Details, Add/Edit Item, Savings Plan editor, Add Deposit/Withdrawal, Transaction History, Purchased Archive, Backup/Restore. Purchased Archive can be reached via Home or Settings; choose one discoverable permanent entry during UI design.

**Primary actions:** Floating Add button on Home/Collections. Item detail has Save Money, Withdraw, Set/Edit Plan, Edit Item, Mark Purchased, View History. Explicitly distinguish *planned* contributions from *actual recorded* deposits.

## 6. Accounting invariants and event model

Treat savings as virtual earmarked balances, **not a real bank account**.

Event types:

- `DEPOSIT`: +amount
- `WITHDRAWAL`: −amount
- `PURCHASE_SPEND`: −amount (created only when marking purchased; links to purchase record)

Available balance at any point = sum of signed events for that item. All event values are positive integer IDR amounts and typed separately; use overflow-safe integer arithmetic. Purchase completion writes `PURCHASE_SPEND(availableBalanceBeforePurchase)`, so available becomes 0. This preserves full inflow/outflow history and prevents the archive from pretending money remains available. **Never classify a purchase-spend as a normal withdrawal in UI.**

Additional invariants:

- Every item and ledger entry uses IDR; no currency columns or conversion behavior are needed.
- Amount > 0 for all user-entered transactions; `PURCHASE_SPEND` event of zero can be omitted when there are no funds, while still recording a purchase record.
- No ordinary withdrawal makes available balance negative.
- Multi-step purchase completion is atomic.
- Purchased items contribute 0 to active totals even if old ledger entries remain queryable.
- Excess savings allowed until purchase; progress display capped at 100%.

**Potential conceptual issue:** Restoring a purchased item without a new savings cycle means it will retain historic transactions, with zero current balance after purchase. This behavior is well-defined and implementable, but a future version may model multiple savings/purchase cycles as separate goals to avoid confusing history.

## 7. Data model (proposed)

Use Room entities and explicit migrations. Logical schema below; implementation may normalize further.

```text
Collection(
  id UUID PK,
  name TEXT NOT NULL,
  createdAt INSTANT,
  updatedAt INSTANT
)

WishlistItem(
  id UUID PK,
  name TEXT NOT NULL,
  targetPriceIdr LONG NOT NULL,
  imageRelativePath TEXT NULL,
  purchaseUrl TEXT NULL,
  notes TEXT NULL,
  category TEXT NULL,
  collectionId UUID NULL FK Collection ON DELETE SET NULL,
  status ENUM(ACTIVE,PURCHASED),
  createdAt INSTANT,
  updatedAt INSTANT,
  purchasedAt INSTANT NULL
)

SavingsEvent(
  id UUID PK,
  itemId UUID NOT NULL FK WishlistItem ON DELETE CASCADE,
  type ENUM(DEPOSIT,WITHDRAWAL,PURCHASE_SPEND),
  amountIdr LONG NOT NULL,
  note TEXT NULL,
  createdAt INSTANT,
  purchaseRecordId UUID NULL
)

SavingsPlan(
  id UUID PK,
  itemId UUID UNIQUE NOT NULL FK WishlistItem ON DELETE CASCADE,
  contributionIdr LONG NOT NULL,
  frequency ENUM(DAILY,WEEKLY,MONTHLY),
  reminderEnabled BOOLEAN,
  reminderLocalTime LOCAL_TIME NULL,
  active BOOLEAN,
  createdAt INSTANT,
  updatedAt INSTANT
)

PurchaseRecord(
  id UUID PK,
  itemId UUID NOT NULL FK WishlistItem ON DELETE CASCADE,
  completedAt INSTANT,
  savingsSpentIdr LONG NOT NULL
)

AppPreferences(
  themeMode ENUM(SYSTEM,LIGHT,DARK),
  accentColor TEXT NULL,
  useDynamicColor BOOLEAN,
  summaryCadence ENUM(OFF,WEEKLY,MONTHLY),
  ...
)
```

**Note:** IDR amounts are stored as integer rupiah (`Long`), not in a variable minor-unit scale. Validate overflows in additions, subtractions, multiplication, and aggregate queries. Kotlin/Room adapters must explicitly handle UUID, Instant, LocalDate, and enum representations.

## 8. Technical architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Navigation:** Navigation Compose
- **State:** ViewModel + StateFlow/Flow
- **Architecture:** Presentation → Domain/use cases → Data/repositories; MVVM UI; single local source of truth
- **Persistence:** Room (items, collections, events, plans, purchase records); DataStore (preferences)
- **Dependency injection:** Hilt
- **Concurrency:** Coroutines + Flow
- **Images:** Android Photo Picker / SAF + app-owned image storage; Coil for display
- **Background:** WorkManager for deferrable reminders and summaries, with OS timing disclaimers
- **Backups:** Storage Access Framework + versioned ZIP/JSON package

**Locked compatibility requirement:** `minSdk = 31` (**Android 12**). Use stable compile/target SDK and dependency versions verified at implementation time; check Google Play target SDK requirements before publishing.

### Recommended code modules (logical, not mandatory Gradle modules)

```text
app/
  presentation/
    home/ collections/ items/ savings/ archive/ settings/
  domain/
    model/ usecase/ calculator/
  data/
    local/ repository/ backup/ notification/
  di/
  ui/theme/
```

Business calculations (savings, projections, dashboard aggregations) must not depend on Android UI APIs, enabling unit tests.

## 9. IDR monetary formatting and precision policy

- All prices, savings events, plan contributions, spend records, and dashboard values are stored and displayed in IDR only.
- All amounts are integral rupiah (`Long`); no fractional amount inputs in MVP.
- Present numbers in Indonesian Rupiah notation (`Rp 1.500.000`) using locale-aware formatting while retaining integer storage.
- Never silently interpret an IDR entry as another currency.
- Ensure input parsing rejects invalid characters, fractions, negative values where prohibited, and values outside supported bounds.
- Use checked integer arithmetic (`Math.addExact`/`Math.subtractExact` or equivalent safeguards) for ledger and aggregation calculations; fail safely on overflow.
- Remove all currency selectors, conversion fields, exchange-rate repository and API calls from implementation scope.

## 10. Notifications policy

- Notifications are optional; permission denial must not affect core app functionality.
- Daily/weekly/monthly contribution reminders are associated with active savings plans; user configures the reminder time. **Plan calculation begins today even if the first reminder cannot be delivered today.**
- Goal reached notification is triggered by crossing threshold after an **actual** deposit or other valid ledger change, not when time passes in a planned schedule.
- Summary cadence weekly/monthly, configurable; include only active goals by default.
- Any OS battery scheduling limitations are reflected in UX: reminders are best-effort unless product explicitly adopts exact alarms and satisfies platform policies.

## 11. Nonfunctional requirements

- **Offline:** All app features, including item, collection, savings, archive, estimate, theme, and backup workflows, work without internet.
- **Performance:** Smooth scrolling in lists of 1,000 items; use lazy lists, paging if needed, and background IO.
- **Integrity:** Atomic ledger operations and purchase completion; migration and backup validation; no floating-point monetary balances.
- **Privacy:** No login or bank credentials; no analytics SDK required for MVP; no network service is required for core or currency functionality.
- **Accessibility:** Large text, screen-reader labels, contrast, adequate touch targets, not relying solely on color for progress.
- **Resilience:** Clear empty/error/loading states; no crashes on missing image or malformed URL.
- **Localization:** Initially English UI; format all monetary values in IDR and dates using locale-aware formatting; keep architecture ready for translations.

## 12. Test strategy / executable acceptance scenarios

### Unit tests (P0)

- Deposit + withdrawal balance across multiple events.
- Withdrawal equal to balance, greater than balance, zero/negative amount.
- Overfunded item (`saved > target`): raw surplus retained, visual fraction = 1.
- Target price = 0: goal reached, no division by zero.
- Purchase marking: `PURCHASE_SPEND` equals available balance, available becomes 0, historic deposits remain.
- Purchase when savings = 0: no invalid zero-amount event, still archives with purchase record.
- Double-tap/concurrent purchase attempt: at most one purchase completion (idempotent or transaction guard).
- Restored archived item: no reversal of prior spend, available zero after previously fully spent balance.
- Savings plan date today for one contribution; today + 2 weeks for three weekly contributions; end-of-month anchor/clamp; leap years; local date boundary.
- Plan change recalculates projection but never generates deposits.
- IDR-only input validation, overflow-safe ledger and dashboard sums, no fractional rupiah, and locale-aware formatting.

### Integration/UI tests (P0)

- Create item offline, attach image, relaunch, verify data and copied image.
- Delete collection and verify items remain unassigned.
- Record events, restart, verify exact balance.
- Archive item, verify dashboard exclusion, show spend event and history.
- Restore item, verify spend history intact.
- Export/import backup with image, collection mapping, ledger, and theme settings; malformed archive rejected without data loss.
- Denied notification permission leaves app functional.
- Dashboard totals remain complete and accurate offline without currency conversion.
- Theme and accent preferences persist.

## 13. Proposed implementation sequence

1. **Foundation:** Project setup, Compose, theme, nav, Room, DataStore, dependency injection.
2. **Wishlist:** Item CRUD, local images, collections, category, All Items browsing.
3. **Savings:** Auditable ledger, balances, savings plans, completion projections, unit tests.
4. **Purchase lifecycle:** Purchase record, purchase-spend accounting, archive/restoration, transactional tests.
5. **Dashboard:** Summary totals, recent activity, upcoming plans, aggregation tests.
6. **Notifications:** Preferences, permission, schedules, duplicate prevention.
7. **Backup and stabilization:** Versioned import/export, media integrity, instrumentation tests, accessibility and performance.

## 14. Definition of done for personal MVP

- [ ] All P0 user journeys work completely offline.
- [ ] Users can manage 1,000 items without major UI regressions.
- [ ] Exact savings math and estimated dates pass the test cases above.
- [ ] Purchasing an item records spend, retains history, archives the item, and does not corrupt totals.
- [ ] Backup/restore passes a round-trip test including images and transaction history.
- [ ] All monetary data and dashboard aggregates are stored and displayed exclusively in IDR, without conversion.
- [ ] Settings and themes survive restart.
- [ ] Reminder permissions and notification failures degrade gracefully.
- [ ] No known critical data-loss, currency precision, or archive accounting bugs.

## 15. Remaining implementation decisions and blockers

**Explicitly finalized, not blockers:**

- **Currency: IDR only.** No other currency and no exchange-rate service.
- **Purchase accounting: spend the entire available recorded savings balance** when marking an item purchased, even when it differs from the item's target price; do not invent deposits or track external checkout funds.
- **Minimum Android version: Android 12 / API 31 (`minSdk = 31`).**
- **Excess savings: retain** before purchase and display goal progress at a visual maximum of 100%.
- **Collections: at most one per item.**
- **Savings projection: first hypothetical contribution today**, without creating an actual deposit.

| Priority | Remaining decision | Default adopted for implementation | Why it matters |
|---|---|---|---|
| Medium | Re-purchasing / restoring previously purchased items | Restore with zero available balance; keep prior purchase spend and all history, append future transactions to same item. | Keeps audit history intact; future version may add explicit goal cycles. |
| Medium | Reminder timing and defaults | User-configurable local reminder time; reminders disabled until user enables them; best-effort OS scheduling. | Android may defer background execution. |
| Medium | Monthly plan scheduling edge cases | Anchor to today's day-of-month and clamp to each destination month's end; recompute from today's date. | Defines Jan 31 / leap-year projections. |
| Medium | Backup restore semantics | Confirmed complete replacement after staged validation, never merge. | Prevents duplicated ledger entries and inconsistent relationships. |
| Low | Categories | Free-text category with suggested labels. | Does not constrain collections. |
| Low | Search/sort/archive navigation | Search by name, sort recently added; archive accessible from Home and Settings. | UX discoverability. |
| Low | Language / branding | English UI, IDR monetary formatting, Material 3 custom palette. | Can be refined without schema changes. |

### Guidance for coding agent

**Do not conflate projections and transactions.** "Start today" only changes the hypothetical completion estimate; it never inserts a deposit.

**Purchase completion must spend the entire currently available recorded savings balance.** If the target price is Rp 1.000.000 and available savings are Rp 600.000, create a `PURCHASE_SPEND` for Rp 600.000 and archive the item. If savings are Rp 1.200.000, spend all Rp 1.200.000. If savings are Rp 0, archive with a purchase record but skip a zero-amount ledger event. Ensure only one purchase event is created per purchase operation.

**IDR is the only currency.** Do not add currency pickers, FX providers, network clients for exchange rates, currency columns or cache tables, or multi-currency test cases. All monetary math is integral IDR with overflow safeguards.

**Android 12 (API 31) is the locked minimum**, not a suggestion. Verify current compile/target requirements independently when creating the Android project.

---

_End of PRD v1.2._
