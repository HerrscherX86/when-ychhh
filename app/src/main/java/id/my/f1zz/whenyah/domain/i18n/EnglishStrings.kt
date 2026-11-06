package id.my.f1zz.whenyah.domain.i18n

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.util.Locale

object EnglishStrings : AppStrings {
    override val locale: Locale = Locale.ENGLISH

    override val appName = "When Yah"
    override val back = "Back"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val save = "Save"
    override val create = "Create"
    override val edit = "Edit"
    override val remove = "Remove"
    override val none = "None"
    override val add = "Add"
    override val off = "Off"
    override val weekly = "Weekly"
    override val monthly = "Monthly"
    override val active = "Active"
    override val paused = "Paused"
    override val goalReached = "Goal reached"
    override val fullyFunded = "Fully funded"
    override val plannedNotPaid = "Planned, not paid"
    override val noEstimateAvailable = "No estimate available"
    override val dateTooFar = "The projected date is too far in the future"
    override val itemNotFound = "Item not found"
    override val itemMayBeDeleted = "It may have been deleted."
    override val deposit = "Deposit"
    override val withdrawal = "Withdrawal"
    override val spentOnPurchase = "Spent on purchase"

    override val navHome = "Home"
    override val navCollections = "Collections"
    override val navSettings = "Settings"
    override val titleItemDetails = "Item details"
    override val titleTransactionHistory = "Transaction history"
    override val titleSavingsPlan = "Savings plan"
    override val titleAddItem = "Add item"
    override val titleEditItem = "Edit item"
    override val titleCollection = "Collection"
    override val titlePurchasedArchive = "Purchased archive"
    override val titleArchivedItem = "Archived item"
    override val titleActivity = "Activity"

    override val totalSaved = "Total saved"
    override val archiveShort = "Archive"
    override val overallProgress = "Overall progress"
    override val activeItems = "Active items"
    override val totalTarget = "Total target"
    override val fullyFundedLabel = "Fully funded"
    override val noActiveItemsYet = "No active items yet"
    override val noTargetAmountSet = "No target amount set"
    override val allActiveGoalsFunded = "All active goals funded"
    override val recentActivity = "Recent activity"
    override val viewAll = "View all"
    override val upcomingPlans = "Upcoming plans"
    override val closestGoals = "Closest goals"
    override val allItems = "All items"
    override val searchItems = "Search items"
    override val sortRecent = "Recent"
    override val sortName = "Name"
    override val noItemsYet = "No items yet"
    override val noItemsMessage = "Tap + to add your first wishlist item."
    override val noMatchingItems = "No matching items"
    override val noMatchingMessage = "Try a different search."
    override val addItem = "Add item"
    override val addItemDescription = "Add item"
    override val noActivityYet = "No activity yet"
    override val noActivityMessage = "Deposits and withdrawals from active items will appear here."

    override val noCollectionsYet = "No collections yet"
    override val noCollectionsMessage = "Create a collection to group your wishlist items."
    override val collectionNameLabel = "Collection name"
    override val nameRequired = "Name is required"
    override val renameCollection = "Rename collection"
    override val deleteCollection = "Delete collection"
    override val addCollectionDescription = "Add collection"
    override val newCollection = "New collection"
    override val deleteCollectionTitle = "Delete collection?"
    override val collectionNotFound = "Collection not found"
    override val noItemsInCollection = "No items in this collection"
    override val noItemsInCollectionMessage = "Add an item to start this collection."

    override val markPurchased = "Mark purchased"
    override val markPurchasedTitle = "Mark purchased?"
    override val collectionLabel = "Collection"
    override val categoryLabel = "Category"
    override val addedLabel = "Added"
    override val updatedLabel = "Updated"
    override val notesLabel = "Notes"
    override val openPurchaseLink = "Open purchase link"
    override val invalidPurchaseLink = "This purchase link is invalid."
    override val noAppCanOpenLink = "No app can open this link."
    override val noSavingsActivityYet = "No savings activity yet."
    override val deleteItemTitle = "Delete item?"
    override val deleteItemMessage = "This removes the item, its image, and all related savings data. This cannot be undone."
    override val savings = "Savings"
    override val targetMet = "Target met"
    override val addSavings = "Add savings"
    override val withdraw = "Withdraw"
    override val savingsPlan = "Savings plan"
    override val noPlanMessage = "No plan yet. Set a contribution amount and cadence to estimate completion."
    override val setAPlan = "Set a plan"
    override val planPaused = "Plan paused"
    override val resumePlanToEstimate = "Resume the plan to see its completion estimate."
    override val editPlan = "Edit plan"
    override val addSavingsTitle = "Add savings"
    override val withdrawSavingsTitle = "Withdraw savings"
    override val amountIdr = "Amount (IDR)"
    override val noteOptional = "Note (optional)"
    override val purchaseNoSavingsInfo = "No savings are recorded, so the item will be archived without a spend event. It does not process a real payment."
    override val markPurchasedConfirm = "Mark purchased"

    override val contributionAmountIdr = "Contribution amount (IDR)"
    override val frequency = "Frequency"
    override val contributionReminder = "Contribution reminder"
    override val reminderHelp = "Best-effort reminder. Requires notification permission."
    override val createPlan = "Create plan"
    override val updatePlan = "Update plan"
    override val planStatus = "Plan status"
    override val pausePlan = "Pause plan"
    override val resumePlan = "Resume plan"
    override val deletePlan = "Delete plan"
    override val deletePlanTitle = "Delete savings plan?"
    override val deletePlanMessage = "The saved contributions stay in the ledger. Only the plan and its estimate are removed."
    override val projection = "Projection"
    override val enterContributionEstimate = "Enter a contribution amount to see an estimate."
    override val estimateAssumes = "Estimate assumes contributions beginning today and continuing on schedule."
    override val enterValidContribution = "Enter a valid contribution greater than zero"
    override val couldNotSavePlan = "Could not save the plan"
    override val couldNotUpdatePlan = "Could not update the plan"
    override val couldNotDeletePlan = "Could not delete the plan"

    override val selectedItemImage = "Selected item image"
    override val chooseImage = "Choose image"
    override val changeImage = "Change image"
    override val nameLabel = "Name"
    override val targetPriceIdr = "Target price (IDR)"
    override val categoryOptional = "Category (optional)"
    override val noCollection = "No collection"
    override val purchaseUrlOptional = "Purchase URL (optional)"
    override val notesOptional = "Notes (optional)"
    override val saveChanges = "Save changes"
    override val couldNotLoadImage = "Could not load the selected image"
    override val enterItemName = "Enter an item name"
    override val enterValidIdr = "Enter a valid IDR amount using digits only"
    override val enterValidUrl = "Enter a valid http(s) URL"
    override val couldNotSaveItem = "Could not save the item"

    override val allTransactions = "All transactions"
    override val noTransactionsYet = "No transactions yet"
    override val noTransactionsMessage = "Add savings or withdraw from this item to build its history."

    override val noPurchasedItemsYet = "No purchased items yet"
    override val noPurchasedItemsMessage = "Items you mark as purchased will appear here with their savings history."
    override val purchaseDateUnavailable = "Purchase date unavailable"
    override val purchaseDetailsUnavailable = "Purchase details unavailable"
    override val purchaseRecord = "Purchase record"
    override val purchaseRecordUnavailable = "Purchase record unavailable."
    override val savedBeforePurchase = "Saved before purchase"
    override val spentFromSavingsLabel = "Spent from savings"
    override val savingsSpentDisclaimer = "Savings spent is the amount you set aside, not necessarily the full purchase price."
    override val transactionHistory = "Transaction history"
    override val noLedgerEntries = "No ledger entries."
    override val restoreToActive = "Restore to active"
    override val deletePermanently = "Delete permanently"
    override val deleteArchivedItemTitle = "Delete archived item?"
    override val deleteArchivedItemMessage = "This permanently deletes the item, image, ledger, plan, and purchase record. It cannot be undone."
    override val couldNotRestoreItem = "Could not restore the item"
    override val couldNotDeleteItem = "Could not delete the item"

    override val appearance = "Appearance"
    override val theme = "Theme"
    override val system = "System"
    override val light = "Light"
    override val dark = "Dark"
    override val dynamicColor = "Dynamic color"
    override val dynamicColorHelp = "Use the device wallpaper palette when available."
    override val accentColor = "Accent color"
    override val accentHelp = "Turn off dynamic color to use a custom accent."
    override val accentDefault = "Default"
    override val accentBlue = "Blue"
    override val accentGreen = "Green"
    override val accentAmber = "Amber"
    override val accentRose = "Rose"
    override val notifications = "Notifications"
    override val enableNotifications = "Enable notifications"
    override val permissionDenied = "Permission denied. Enable notification permission in system settings."
    override val notificationBestEffort = "Reminders and summaries are best-effort and may be delayed by Android."
    override val goalNotifications = "Goal reached"
    override val goalNotificationsHelp = "Notify when an actual deposit reaches an item's target."
    override val contributionReminderHelp = "Contribution reminders are configured per plan in the savings-plan editor."
    override val progressSummary = "Progress summary"
    override val data = "Data"
    override val purchasedArchive = "Purchased archive"
    override val exportBackup = "Export backup"
    override val exporting = "Exporting…"
    override val restoreBackup = "Restore backup"
    override val restoring = "Restoring…"
    override val restoreReplacesData = "Restore replaces all local data. Backups use Android's file picker, so no storage permission is needed."
    override val about = "About"
    override val appVersion = "When Yah 1.0"
    override val appTagline = "Offline-first wishlist and savings tracker • IDR only"
    override val language = "Language"
    override val languageHelp = "Choose the language used across the app."
    override val languageEnglish = "English"
    override val languageIndonesian = "Bahasa Indonesia"
    override val backupExportFailed = "Could not export the backup."
    override val backupRestoreFailed = "Could not restore the backup."
    override val restoreBackupTitle = "Restore backup?"
    override val restoreBackupMessage = "This replaces all items, collections, savings, plans, purchase records, images, and settings. This cannot be undone. Export a backup first if needed."
    override val restore = "Restore"

    override val notificationChannelReminders = "Contribution reminders"
    override val notificationChannelRemindersDescription = "Reminders for savings-plan contributions"
    override val notificationChannelGoals = "Goal reached"
    override val notificationChannelGoalsDescription = "Notifications when an item reaches its savings goal"
    override val notificationChannelSummaries = "Progress summaries"
    override val notificationChannelSummariesDescription = "Periodic savings progress summaries"
    override val summaryNotificationTitle = "Savings progress"

    override val couldNotRecordTransaction = "Could not record transaction"
    override val withdrawalExceeds = "Withdrawal exceeds available savings"
    override val enterValidAmount = "Enter a valid amount greater than zero"
    override val couldNotMarkPurchased = "Could not mark the item purchased"

    override fun itemCount(count: Int): String =
        if (count == 1) "1 item" else "$count items"

    override fun transactionCount(count: Int): String = "$count transactions"

    override fun viewAllTransactions(count: Int): String = "View all $count transactions"

    override fun targetAndSaved(target: String, saved: String): String =
        "Target $target • Saved $saved"

    override fun targetLabel(value: String): String = "Target: $value"

    override fun savedLabel(value: String): String = "Saved: $value"

    override fun availableLabel(value: String): String = "Available: $value"

    override fun remainingLabel(value: String): String = "$value remaining"

    override fun percentageSaved(percent: Int): String = "$percent% saved"

    override fun percentageFunded(percent: Int): String = "$percent% funded"

    override fun contributionCount(count: Long): String =
        if (count == 1L) "1 planned contribution" else "$count planned contributions"

    override fun estimatedCompletion(date: String): String = "Estimated completion: $date"

    override fun completionDate(date: String): String = "Completion date: $date"

    override fun purchasedOn(date: String): String = "Purchased $date"

    override fun spentFromSavings(value: String): String = "Spent from savings $value"

    override fun amountLeft(value: String): String = "$value left"

    override fun ofTarget(value: String): String = "of $value"

    override fun aboveTarget(value: String): String = "$value above target"

    override fun contributionsEstimate(count: Long, date: String): String =
        "$count contributions · est. $date"

    override fun deleteCollectionMessage(name: String): String =
        "Items in \"$name\" will be kept and unassigned from it."

    override fun purchaseSpendInfo(amount: String): String =
        "This records a purchase spend of $amount and moves the item to the archive. " +
            "It does not process a real payment."

    override fun reminderTimeLabel(time: String): String = "Reminder time: $time"

    override fun completionDateLabel(date: String): String = "Completion date: $date"

    override fun remainingValue(value: String): String = "Remaining: $value"

    override fun estimatedCompletionLabel(date: String): String = "Estimated completion: $date"

    override fun summaryTimeLabel(time: String): String = "Summary time: $time"

    override fun backupExported(items: Int, images: Int): String =
        "Exported $items items and $images images."

    override fun backupRestored(items: Int, images: Int): String =
        "Restored $items items and $images images."

    override val backupSettingsFailedSuffix = " Settings could not be restored."

    override fun planReminderTitle(itemName: String): String = "Time to save for $itemName"

    override fun planReminderMessage(amount: String, frequency: PlanFrequency): String =
        "$amount ${planFrequency(frequency).lowercase()} contribution. " +
            "Planned contributions are not deducted automatically."

    override fun goalNotificationTitle(itemName: String): String = "Goal reached: $itemName"

    override fun goalNotificationMessage(saved: String, target: String): String =
        "Saved $saved of $target."

    override fun summaryNotificationMessage(active: Int, saved: String, funded: Int): String {
        val itemLabel = if (active == 1) "active item" else "active items"
        return "$active $itemLabel • $saved saved • $funded funded"
    }

    override fun planFrequency(frequency: PlanFrequency): String = when (frequency) {
        PlanFrequency.DAILY -> "Daily"
        PlanFrequency.WEEKLY -> "Weekly"
        PlanFrequency.MONTHLY -> "Monthly"
    }
}
