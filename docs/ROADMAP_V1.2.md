# When Yah — v1.2 Roadmap

Status: **implemented**. Track A (Finishing Touches) and Track B (Find & Organize) are both committed
in the working tree; the app version has not yet been bumped to 1.2.

- Base release: v1.1 (`versionCode = 2`, `versionName = "1.1"`)
- Recommended theme: **Finishing Touches**
- Optional second track: **Find & Organize**
- Product source of truth: `wishlist_savings_tracker_PRD_v1.2.md`
- Engineering rules: `AGENTS.md`

---

## Why this release

v1.1 fixed reminder scheduling and added the GitHub update checker. v1.2 should make the existing app feel finished:

- Close the deferred interaction-feedback work.
- Cover the PRD accessibility requirements.
- Make notification reliability understandable on aggressive Android skins.
- Polish the update flow and a few rough UI edges.
- Avoid new database or ledger behavior.

If v1.1 was “make it work”, v1.2 is “make it feel right”.

---

## Track A — Finishing Touches (recommended v1.2)

### A1. Interaction feedback

Add a consistent feedback layer for every meaningful action.

- [ ] Add a shared `SnackbarHostState` and `SnackbarHost` to the app scaffold.
- [ ] Show a snackbar after:
  - item created / updated / deleted
  - collection created / renamed / deleted
  - deposit / withdrawal recorded
  - item marked purchased
  - backup exported / restored
  - backup export / restore failure
- [ ] Add haptic feedback on:
  - successful save
  - destructive confirmations (delete, purchase)
  - backup success / failure
- [ ] Make save buttons show an in-progress state instead of silently closing.
- [ ] Focus the first invalid field and scroll to it in:
  - `ItemEditorScreen`
  - `SavingsPlanScreen`
- [ ] Keep existing inline backup progress/messages; snackbars are for transient completion.
- [ ] Add screen-reader live-region announcements for the same actions.

Key files:

```text
ui/WhenYahApp.kt
ui/home/HomeScreen.kt
ui/collections/CollectionsScreen.kt
ui/collections/CollectionDetailScreen.kt
ui/item/ItemEditorScreen.kt
ui/item/ItemDetailScreen.kt
ui/item/SavingsPlanScreen.kt
ui/settings/SettingsScreen.kt
ui/components/Dialogs.kt
ui/components/ItemCard.kt
```

Acceptance criteria:

- No action closes a dialog silently.
- Errors are announced and visible, not only color-coded.
- Haptics follow the system setting.
- Save flows cannot be double-submitted.

---

### A2. Accessibility pass

The PRD requires large text, screen-reader labels, contrast, adequate touch targets, and not relying only on color.

- [ ] Audit every icon-only button for a meaningful `contentDescription`.
- [ ] Add progress semantics to savings/progress bars, e.g. “48% of target”.
- [ ] Verify TalkBack reads item cards, plan cards, savings summaries, and dialogs sensibly.
- [ ] Test at **200% font scale** and fix clipping/overflow:
  - top app bar titles
  - filter chips
  - settings rows
  - item/plan cards
  - buttons with long Indonesian labels
- [ ] Ensure touch targets are at least 48 dp.
- [ ] Check contrast in light, dark, and dynamic color:
  - `primaryContainer` surfaces
  - alpha text on containers
  - outlined buttons and disabled states
- [ ] Verify reduced-motion behavior; do not rely on animation to communicate state.
- [ ] Keep the percentage label alongside every progress bar.

Key files:

```text
ui/components/ItemCard.kt
ui/components/SavingsEventRow.kt
ui/home/HomeScreen.kt
ui/item/ItemDetailScreen.kt
ui/item/TransactionHistoryScreen.kt
ui/settings/SettingsScreen.kt
ui/onboarding/OnboardingScreen.kt
ui/theme/*
```

Acceptance criteria:

- Main flows usable with TalkBack at 200% font scale.
- No critical text truncated beyond recovery.
- Progress is understandable without seeing the bar.

---

### A3. Reminder reliability

v1.1 now uses `AlarmManager` with exact alarms when allowed. Make that visible and actionable.

- [ ] Add a “Reminder reliability” block in Settings → Notifications.
- [ ] Show exact-alarm status (`AlarmManager.canScheduleExactAlarms()`).
- [ ] Show battery-optimization status (`PowerManager.isIgnoringBatteryOptimizations`).
- [ ] Add a button to open the battery-optimization settings list:
  - `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`
  - Do **not** request `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`; Play policy is restrictive and the direct request is unnecessary.
- [ ] Add a generic OEM auto-start help dialog:
  - mention Infinix/Xiaomi/Oppo/Vivo-style battery managers
  - tell users to allow background activity
  - do not deep-link to undocumented vendor activities
- [ ] Add a “Send test notification” action:
  - uses the plan-reminder channel
  - does nothing if permission is denied; explain why
- [ ] Update notification copy:
  - replace “reminders are best-effort” with wording that reflects exact alarms plus OEM limitations
  - update the plan editor’s reminder helper text
- [ ] Keep all functionality working when notification permission is denied.

Key files:

```text
ui/settings/SettingsScreen.kt
ui/settings/SettingsViewModel.kt
data/notification/NotificationService.kt
data/notification/ReminderAlarmScheduler.kt
domain/i18n/AppStrings.kt
domain/i18n/EnglishStrings.kt
domain/i18n/IndonesianStrings.kt
```

Acceptance criteria:

- Users can see why a reminder might be delayed.
- Exact-alarm permission is easy to grant.
- A test notification proves the channel/permission setup.
- No new permissions are added beyond what is already needed.

---

### A4. Update-flow polish

- [ ] Add **“Skip this version”** to the update dialog.
- [ ] Keep the existing **Later** behavior: 3-day snooze.
- [ ] `Skip this version` suppresses automatic prompts for that `versionCode`.
- [ ] Manual “Check for updates” still shows an available update even if skipped.
- [ ] Store `skippedVersionCode` in `UpdatePreferences` (device-local, not backed up).
- [ ] Add unit tests for skip + snooze + manual override.
- [ ] Keep the dialog opening the GitHub release page; do not add silent APK installation.
- [ ] Make release notes readable; truncate very long notes with “read more” only if needed.

Key files:

```text
data/update/UpdateManager.kt
data/update/UpdatePreferences.kt
domain/update/UpdatePolicy.kt
ui/components/UpdateAvailableDialog.kt
app/src/test/java/id/my/f1zz/whenyah/UpdatePolicyTest.kt
```

Acceptance criteria:

- A skipped version does not prompt automatically.
- A new version prompts immediately.
- A manual check always gives a clear result.
- No change to the release-asset URL contract.

---

### A5. Small UI fixes

- [ ] Audit the Home FAB against interactive content.
  - The FAB can overlap the search field at some scroll positions.
  - Consider a bottom spacer or hiding the FAB while the search field is focused/visible.
- [ ] Use the same IDR grouping in the plan editor as the item editor.
  - Current plan editor shows `10000`; it should read `10.000` while editing.
  - Reuse the existing formatter/parser behavior.
- [ ] Fix any large-font truncation found in A2.
- [ ] Verify empty/loading/error states at small screen sizes.
- [ ] Check collection/archive lists after deleting items.

Key files:

```text
ui/home/HomeScreen.kt
ui/item/SavingsPlanScreen.kt
ui/item/ItemEditorScreen.kt
ui/util/Formatters.kt
ui/components/ItemCard.kt
ui/collections/CollectionDetailScreen.kt
ui/archive/ArchiveScreen.kt
```

Acceptance criteria:

- The FAB never covers a field the user is expected to tap.
- IDR inputs look consistent across editors.
- No clipped primary actions at 200% font.

---

### A6. Copy consistency

- [ ] Update all “best-effort” wording to match exact alarms + OEM limitations.
- [ ] Update README/AGENTS if behavior or permissions change.
- [ ] Keep English and Indonesian strings in sync.
- [ ] Use `strings.locale` for dates and number formatting.
- [ ] No hardcoded user-facing copy.

---

### Optional A7. Data-safety polish

Only if the release has room:

- [ ] Show “Last backup: X days ago” in Settings.
- [ ] Gentle reminder if there has been no backup for 30+ days.
- [ ] Do not block core features.
- [ ] Keep backup date out of backups (device-local).

---

## Track B — Find & Organize (optional / v1.3)

If you want a more visibly feature-focused release, add this track. It is larger than Track A and should probably be its own release.

### B1. Filters and sort

- [x] Filter Home by:
  - collection
  - category
  - status (active, goal reached)
  - has plan / no plan
- [x] Add sort options:
  - progress
  - target price
  - recently updated
- [x] Keep search + existing recent/name sort.
- [x] Decide whether filter state persists across restarts (recommend: no for now).

Implementation notes:

- Filtering can be done in `HomeViewModel` on the already-loaded list; no schema change.
- Keep a pure filter/sort function in `domain/` and unit-test it.

Implemented in `domain/wishlist/WishlistQuery.kt` (`WishlistQueryEngine`, `ItemSortOrder`,
`GoalFilter`, `PlanFilter`) with `WishlistQueryTest`. Filters apply to the All Items list only;
dashboard totals and other Home sections stay global. Filter UI is a `ModalBottomSheet`.

### B2. Collection totals

- [x] Show total target, total saved, and progress on collection cards.
- [x] Show the same summary in collection detail.
- [x] Include only active items.
- [x] Add a small domain calculator + tests.

Implemented by reusing `domain/dashboard/DashboardCalculator.summarize` instead of adding a duplicate
collection calculator; active-only filtering comes from the existing DAO queries.

### B3. Ready to buy

- [x] Add a Home section for fully funded active items.
- [x] Use the existing dashboard summary data; do not change purchase accounting.
- [x] Tapping an item opens detail so the user can mark it purchased.

Implemented as a horizontal carousel (`ReadyToBuyRow`) reusing the closest-goals card. Items with a zero
target are excluded from the section.

### B4. Quick deposit

- [x] Add “Save Rp 10.000” (the plan contribution) on the plan/detail surface.
- [x] Pre-fill the existing transaction dialog; the user can still edit the amount.
- [x] Do not automatically create a ledger event.
- [x] Keep the append-only ledger rules unchanged.

Implemented as a third button in the item-detail savings card; it only seeds
`ItemDetailViewModel.startTransaction(type, initialAmountIdr)`.

### B5. Category suggestions

- [x] Show existing categories as selectable chips in the item editor.
- [x] Keep free-text entry.
- [x] Do not create a separate category table.

Implemented from the already-loaded active items; no schema change.

---

## Recommended order

If implementing Track A:

1. A5 small UI fixes (quick wins, screenshots)
2. A1 feedback infrastructure
3. A2 accessibility
4. A3 reminder reliability
5. A4 update-flow polish
6. A6 copy consistency
7. Optional A7 backup nudge
8. Tests, release notes, version bump

If implementing Track B as well:

1. Finish Track A.
2. B2 collection totals (small, self-contained)
3. B1 filters/sort
4. B3 ready to buy
5. B4 quick deposit
6. B5 category suggestions

---

## Testing checklist

### Unit tests

- Update policy: skip, snooze, manual override, new version.
- Collection totals calculator.
- Filter/sort logic.
- IDR grouping/parsing in the plan editor.
- Existing savings, plan, purchase, dashboard, backup, and notification tests must stay green.

### Compose / manual

- Snackbar appears after save/delete/purchase/backup.
- Save button cannot be submitted twice.
- Invalid form focuses the first bad field.
- TalkBack reads the main flows.
- 200% font scale: Home, detail, editor, plan, Settings, dialogs.
- Notification permission denied: app still works; test-notification action explains.
- Exact-alarm permission denied: reminders fall back; reliability card explains.
- Offline: update check fails silently; manual check shows an error.
- Update dialog: Later, Skip this version, Update.
- Backup/restore with images and settings.
- Purchase + restore history.
- 1,000-item list scrolling (PRD NFR), if feasible.

---

## Release mechanics

v1.2 is the first release that v1.1 users can auto-detect.

1. Bump `app/build.gradle.kts`:

```kotlin
versionCode = 3
versionName = "1.2"
```

2. Update `docs/update.json`:

```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "releaseUrl": "https://github.com/HerrscherX86/when-ychhh/releases/latest",
  "notes": "What changed in v1.2."
}
```

3. Build and sign with the **same v1.0 keystore**. See `docs/RELEASING.md`.
4. Publish a normal (not prerelease) GitHub release.
5. Attach the signed APK and `update.json`.
6. Test the automatic prompt on a v1.1 install (it sees `versionCode 2 < 3`).
7. Verify a v1.1 → v1.2 install preserves data.

---

## Non-goals / avoid

- Cloud sync, accounts, bank integration, payments, shared wallets.
- Multi-currency or FX.
- Automatic checkout or price scraping.
- Silent APK installation / `REQUEST_INSTALL_PACKAGES`.
- Forced updates.
- Encrypted backups unless separately designed (password/key UX, recovery, restore compatibility).
- Shared-element transitions everywhere or a large visual rewrite.
- New database schema changes unless a feature truly requires them.

---

## Open decisions

- Should Track B be part of v1.2, or a separate v1.3? Recommendation: **v1.3**.
- Should “Skip this version” be permanent for that version? Recommendation: **yes**, automatic prompts only; manual check still shows it.
- Should filters persist across restarts? Recommendation: **no** for the first version.
- Quick deposit: use the plan amount or the last deposit amount? Recommendation: **plan amount, editable**.
- Backup reminder threshold: 30 days? Recommendation: **30 days**, non-blocking.
- Battery settings: open the settings list, not a direct exemption request. Recommendation: **settings list only**.
