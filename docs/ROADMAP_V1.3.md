# When Yah — v1.3 Roadmap

Status: **implemented in the working tree**; not committed or published. Version is bumped to
`versionCode = 4` / `versionName = "1.3"`.

- Base release: v1.2 (`versionCode = 3`)
- Theme: **Daily-Use Polish**
- Product source of truth: `wishlist_savings_tracker_PRD_v1.2.md`
- Engineering rules: `AGENTS.md`
- Detailed state: `docs/agent-handoff.md`

---

## Why this release

v1.2 completed the feature set. v1.3 makes the app useful outside its own window and comfortable on
larger devices without adding backend, schema, or currency complexity:

- A home-screen widget for a quick glance at total savings, overall progress, and active/funded counts.
- A notification action that jumps straight into a pre-filled deposit dialog.
- A navigation rail, responsive grids, and readable content widths for tablets/foldables.
- Predictive back enabled instead of force-disabled.

No database schema change, no backup-format change, no new permissions, no new network calls.

---

## Workstream 1 — Home-screen widgets

- [x] Three widget styles: **Overview** (total saved, progress, active/funded counts), **Closest goal**
      (nearest goal name, amount left, progress), and **Ready to buy** (up to three funded items).
- [x] Shared `WhenYahWidgetBase` + pure `WidgetSnapshotFactory` read model with unit tests.
- [x] Each style has its own `GlanceAppWidgetReceiver` and `res/xml/*_info.xml`.
- [x] Tapping the widget opens Home; tapping the closest goal or a ready-to-buy item opens that item.
- [x] Empty states with an “Add item” action.
- [x] Widget metadata and English/Indonesian widget labels for every style.
- [x] `WidgetUpdater.refresh()` updates all three widgets after item, ledger, purchase, restore,
      backup, language, and app-start changes.
- [x] Settings → Appearance can pin any style through `WhenYahWidgetType`.
- [x] Widget copy follows `SettingsRepository.language`, not the process `LanguageStore`.

Key files:

```text
data/widget/WidgetSnapshot.kt
data/widget/WidgetSnapshotFactory.kt
data/widget/WhenYahWidgetBase.kt
data/widget/WhenYahWidget.kt
data/widget/ClosestGoalWidget.kt
data/widget/ReadyToBuyWidget.kt
data/widget/WidgetCommon.kt
data/widget/*WidgetReceiver.kt
data/widget/WidgetUpdater.kt
di/WidgetEntryPoint.kt
res/xml/whenyah_widget_info.xml
res/xml/whenyah_widget_closest_info.xml
res/xml/whenyah_widget_ready_info.xml
```

Acceptance:

- All three styles appear in the launcher’s widget picker.
- Widget totals and progress match Home; closest-goal and ready-to-buy data are consistent with Home.
- Every style updates after a deposit/withdrawal/purchase/restore and after app restart.
- Widgets never mutate the ledger or open a dialog by themselves.
- Widget failure never fails the underlying savings write.

---

## Workstream 2 — Notification quick deposit + launch routing

- [x] Plan reminders gain an “Add savings” action.
- [x] The action opens the item detail with the transaction dialog pre-filled with the plan contribution (editable).
- [x] Notification bodies open the item detail without a dialog.
- [x] Goal notifications open the item detail; summaries/tests open Home.
- [x] Pure `LaunchRequestParser` + `AppLauncher` `StateFlow` for intents from notifications and the widget.
- [x] `MainActivity` is `singleTop`, handles cold start once, and forwards `onNewIntent`.
- [x] `Routes.ITEM_DETAIL` accepts an optional `depositAmount` long arg (0 = no dialog).

Key files:

```text
data/navigation/LaunchContract.kt
data/navigation/AppLauncher.kt
data/notification/NotificationService.kt
data/notification/ReminderCoordinator.kt
MainActivity.kt
ui/WhenYahApp.kt
navigation/Routes.kt
ui/item/ItemDetailViewModel.kt
```

Acceptance:

- Cold start from the notification action lands on the right item with the dialog open.
- Warm start while on another screen does the same.
- Rotation does not duplicate the dialog.
- No ledger event is written until the user submits the dialog.

---

## Workstream 3 — Adaptive layout

- [x] `NavigationRail` replaces the bottom bar at ≥600dp on top-level destinations.
- [x] `AdaptiveContent` caps and centers content: Home 960dp, Settings 840dp, detail/form screens 720dp.
- [x] Collections, collection detail, and Archive use `LazyVerticalGrid` with `GridCells.Adaptive`.
- [x] `rememberFabVisibility` supports both `LazyListState` and `LazyGridState`.
- [x] Phone layout remains one column with the bottom bar.

Key files:

```text
ui/components/Adaptive.kt
ui/components/ScrollAwareFab.kt
ui/WhenYahApp.kt
ui/collections/CollectionsScreen.kt
ui/collections/CollectionDetailScreen.kt
ui/archive/ArchiveScreen.kt
```

Acceptance:

- Phone: bottom bar and single-column lists unchanged.
- Tablet/foldable ≥600dp: rail visible, content centered, grids show 1–3 columns.
- Rotation/split-screen does not clip cards or lose scroll state.
- Large-font chips and cards still wrap correctly.

Out of scope: two-pane list-detail layouts (recommended for a later release).

---

## Workstream 4 — Predictive back

- [x] `android:enableOnBackInvokedCallback="true"` in the manifest.
- [x] Navigation Compose pop transitions remain pure fades (no scale) for a clean preview.
- [x] Existing `BackHandler`s (dirty editor, onboarding pager) still consume back when needed.
- [ ] Device/emulator gesture verification (pending).

Go/no-go: verify on an API 36 emulator with gesture navigation. If the preview scales or clips,
simplify `popEnterTransition`/`popExitTransition` first; only revert the manifest flag if the
regression persists, and document why.

---

## Tests

New host-side tests:

- `WidgetSnapshotFactoryTest` — empty list, totals, active/funded counts, zero-target handling,
  surplus capping.
- `LaunchRequestParserTest` — item routing, deposit prefill, invalid ids, add-item precedence.

Existing tests stay green. Last verified: **100 tests**, 0 failures.

---

## Manual verification checklist

- [ ] Widget: add from launcher and from Settings; totals match Home; deposit updates it; language switch updates copy; reboot keeps it.
- [ ] Notification action: cold start, warm start, rotation, permission denied.
- [ ] Adaptive: phone, tablet/resizable emulator, split-screen, rotation, 200% font.
- [ ] Predictive back: detail → Home, dirty editor dialog, bottom sheet dismiss, onboarding page back.
- [ ] Backup export/restore still round-trips and refreshes the widget.
- [ ] Offline behavior unchanged.

---

## Release mechanics

1. Confirm `app/build.gradle.kts`: `versionCode = 4`, `versionName = "1.3"`.
2. Confirm `docs/update.json` uses the same version and Indonesian release notes.
3. Build:
   ```bash
   ./gradlew :app:assembleRelease
   ```
4. Sign with the same v1.0–v1.2 keystore (see `docs/RELEASING.md`).
5. Publish a normal (non-prerelease) GitHub release with the signed APK and `update.json`.
6. Verify `releases/latest/download/update.json` and update from v1.2 → v1.3.

---

## Non-goals

- Two-pane list-detail tablet layouts.
- Widget configuration, themes, or per-widget item selection.
- Ledger writes directly from a widget or notification.
- Database schema or backup-format changes.
- Cloud sync, accounts, payments, multi-currency, price scraping.
- R8/minification, CI, encrypted backups.
