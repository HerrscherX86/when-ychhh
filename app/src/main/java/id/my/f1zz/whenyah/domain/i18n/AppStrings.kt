package id.my.f1zz.whenyah.domain.i18n

import id.my.f1zz.whenyah.domain.model.PlanFrequency
import java.util.Locale

interface AppStrings {
    val locale: Locale

    val appName: String
    val onboardingTitle1: String
    val onboardingBody1: String
    val onboardingTitle2: String
    val onboardingBody2: String
    val onboardingTitle3: String
    val onboardingBody3: String
    val onboardingNext: String
    val onboardingStart: String
    val onboardingSkip: String
    val onboardingBack: String
    val back: String
    val cancel: String
    val delete: String
    val save: String
    val create: String
    val edit: String
    val remove: String
    val none: String
    val add: String
    val off: String
    val weekly: String
    val monthly: String
    val active: String
    val paused: String
    val goalReached: String
    val fullyFunded: String
    val plannedNotPaid: String
    val noEstimateAvailable: String
    val dateTooFar: String
    val itemNotFound: String
    val itemMayBeDeleted: String
    val deposit: String
    val withdrawal: String
    val spentOnPurchase: String

    val navHome: String
    val navCollections: String
    val navSettings: String
    val titleItemDetails: String
    val titleTransactionHistory: String
    val titleSavingsPlan: String
    val titleAddItem: String
    val titleEditItem: String
    val titleCollection: String
    val titlePurchasedArchive: String
    val titleArchivedItem: String
    val titleActivity: String

    val totalSaved: String
    val archiveShort: String
    val overallProgress: String
    val activeItems: String
    val totalTarget: String
    val fullyFundedLabel: String
    val noActiveItemsYet: String
    val noTargetAmountSet: String
    val allActiveGoalsFunded: String
    val recentActivity: String
    val viewAll: String
    val upcomingPlans: String
    val closestGoals: String
    val readyToBuy: String
    val allItems: String
    val searchItems: String
    val sortRecent: String
    val sortName: String
    val sortProgress: String
    val sortPrice: String
    val filterAction: String
    val filtersTitle: String
    val filterAll: String
    val filterGoalReached: String
    val filterStillSaving: String
    val filterHasPlan: String
    val filterNoPlan: String
    val filterClearAll: String
    val filterCategory: String
    val filterCollection: String
    val filterStatus: String
    val filterPlan: String
    val clearFilterDescription: String
    val done: String
    val noItemsYet: String
    val noItemsMessage: String
    val noMatchingItems: String
    val noMatchingMessage: String
    val noFilteredItems: String
    val noFilteredItemsMessage: String
    val addItem: String
    val addItemDescription: String
    val noActivityYet: String
    val noActivityMessage: String

    val collectionsOverviewTitle: String
    val organizedItemsLabel: String
    val moreActions: String
    val noCollectionsYet: String
    val noCollectionsMessage: String
    val collectionNameLabel: String
    val nameRequired: String
    val renameCollection: String
    val deleteCollection: String
    val addCollectionDescription: String
    val newCollection: String
    val deleteCollectionTitle: String
    val collectionNotFound: String
    val noItemsInCollection: String
    val noItemsInCollectionMessage: String

    val markPurchased: String
    val markPurchasedTitle: String
    val collectionLabel: String
    val categoryLabel: String
    val addedLabel: String
    val updatedLabel: String
    val notesLabel: String
    val openPurchaseLink: String
    val invalidPurchaseLink: String
    val noAppCanOpenLink: String
    val noSavingsActivityYet: String
    val deleteItemTitle: String
    val deleteItemMessage: String
    val savings: String
    val targetMet: String
    val addSavings: String
    val withdraw: String
    val savingsPlan: String
    val noPlanMessage: String
    val setAPlan: String
    val planPaused: String
    val resumePlanToEstimate: String
    val editPlan: String
    val addSavingsTitle: String
    val withdrawSavingsTitle: String
    val amountIdr: String
    val noteOptional: String
    val purchaseNoSavingsInfo: String
    val markPurchasedConfirm: String

    val contributionAmountIdr: String
    val frequency: String
    val contributionReminder: String
    val reminderHelp: String
    val createPlan: String
    val updatePlan: String
    val planStatus: String
    val pausePlan: String
    val resumePlan: String
    val deletePlan: String
    val deletePlanTitle: String
    val deletePlanMessage: String
    val projection: String
    val enterContributionEstimate: String
    val estimateAssumes: String
    val enterValidContribution: String
    val couldNotSavePlan: String
    val couldNotUpdatePlan: String
    val couldNotDeletePlan: String

    val sectionItemInfo: String
    val sectionOrganization: String
    val sectionAdditionalDetails: String
    val itemPhoto: String
    val tapToChooseImage: String
    val removeImage: String
    val showDetails: String
    val hideDetails: String
    val chooseCollection: String
    val createCollectionAction: String
    val unsavedChangesTitle: String
    val unsavedChangesMessage: String
    val keepEditing: String
    val discardChanges: String
    val saving: String
    val selectedItemImage: String
    val chooseImage: String
    val changeImage: String
    val nameLabel: String
    val targetPriceIdr: String
    val categoryOptional: String
    val categorySuggestionsLabel: String
    val noCollection: String
    val purchaseUrlOptional: String
    val notesOptional: String
    val saveChanges: String
    val couldNotLoadImage: String
    val enterItemName: String
    val enterValidIdr: String
    val enterValidUrl: String
    val couldNotSaveItem: String
    val couldNotCreateCollection: String

    val allTransactions: String
    val noTransactionsYet: String
    val noTransactionsMessage: String

    val noPurchasedItemsYet: String
    val noPurchasedItemsMessage: String
    val purchaseDateUnavailable: String
    val purchaseDetailsUnavailable: String
    val purchaseRecord: String
    val purchaseRecordUnavailable: String
    val savedBeforePurchase: String
    val spentFromSavingsLabel: String
    val savingsSpentDisclaimer: String
    val transactionHistory: String
    val noLedgerEntries: String
    val restoreToActive: String
    val deletePermanently: String
    val deleteArchivedItemTitle: String
    val deleteArchivedItemMessage: String
    val couldNotRestoreItem: String
    val couldNotDeleteItem: String

    val appearance: String
    val theme: String
    val system: String
    val light: String
    val dark: String
    val dynamicColor: String
    val dynamicColorHelp: String
    val accentColor: String
    val accentHelp: String
    val accentDefault: String
    val accentBlue: String
    val accentGreen: String
    val accentAmber: String
    val accentRose: String
    val notifications: String
    val enableNotifications: String
    val permissionDenied: String
    val notificationBestEffort: String
    val allowExactReminders: String
    val reminderReliabilityTitle: String
    val reminderReliabilityHelp: String
    val exactAlarmsAllowed: String
    val exactAlarmsNotAllowed: String
    val batteryOptimizationAllowed: String
    val batteryOptimizationRestricted: String
    val openBatterySettings: String
    val sendTestNotification: String
    val testNotificationSent: String
    val oemHelpTitle: String
    val oemHelpMessage: String
    val openAppSettings: String
    val testNotificationTitle: String
    val testNotificationMessage: String
    val goalNotifications: String
    val goalNotificationsHelp: String
    val contributionReminderHelp: String
    val progressSummary: String
    val data: String
    val purchasedArchive: String
    val exportBackup: String
    val exporting: String
    val restoreBackup: String
    val restoring: String
    val restoreReplacesData: String
    val about: String
    fun appVersion(versionName: String): String
    val appTagline: String
    val updateAvailableTitle: String
    fun updateAvailableMessage(versionName: String): String
    val updateNow: String
    val later: String
    val checkForUpdates: String
    val checkForUpdatesAutomatically: String
    val checkForUpdatesHelp: String
    val updateCheckInProgress: String
    val upToDate: String
    val updateCheckFailed: String
    val skipVersion: String
    val language: String
    val languageHelp: String
    val languageEnglish: String
    val languageIndonesian: String
    val backupExportFailed: String
    val backupRestoreFailed: String
    val restoreBackupTitle: String
    val restoreBackupMessage: String
    val restore: String

    val itemCreated: String
    val itemUpdated: String
    val itemDeleted: String
    val collectionCreated: String
    val collectionRenamed: String
    val collectionDeleted: String
    val depositRecorded: String
    val withdrawalRecorded: String
    val itemPurchased: String
    val planSaved: String
    val planDeleted: String
    val planPausedMessage: String
    val planResumedMessage: String

    val notificationChannelReminders: String
    val notificationChannelRemindersDescription: String
    val notificationChannelGoals: String
    val notificationChannelGoalsDescription: String
    val notificationChannelSummaries: String
    val notificationChannelSummariesDescription: String
    val summaryNotificationTitle: String
    val notificationActionAddSavings: String

    val widgetSectionTitle: String
    val widgetSectionDescription: String
    val widgetAddAction: String
    val widgetAddedMessage: String
    val widgetNotSupportedMessage: String
    val widgetOverview: String
    val widgetClosestGoal: String
    val widgetReadyToBuy: String
    val widgetNothingReady: String
    fun widgetSummary(active: Int, funded: Int): String
    fun widgetReadyCount(count: Int): String
    fun widgetMore(count: Int): String

    val couldNotRecordTransaction: String
    val withdrawalExceeds: String
    val enterValidAmount: String
    val couldNotMarkPurchased: String

    fun itemCount(count: Int): String
    fun transactionCount(count: Int): String
    fun viewAllTransactions(count: Int): String
    fun targetAndSaved(target: String, saved: String): String
    fun targetLabel(value: String): String
    fun savedLabel(value: String): String
    fun availableLabel(value: String): String
    fun remainingLabel(value: String): String
    fun percentageSaved(percent: Int): String
    fun percentageFunded(percent: Int): String
    fun quickSave(amount: String): String
    fun contributionCount(count: Long): String
    fun estimatedCompletion(date: String): String
    fun completionDate(date: String): String
    fun purchasedOn(date: String): String
    fun spentFromSavings(value: String): String
    fun amountLeft(value: String): String
    fun ofTarget(value: String): String
    fun aboveTarget(value: String): String
    fun contributionsEstimate(count: Long, date: String): String
    fun deleteCollectionMessage(name: String): String
    fun purchaseSpendInfo(amount: String): String
    fun reminderTimeLabel(time: String): String
    fun completionDateLabel(date: String): String
    fun remainingValue(value: String): String
    fun estimatedCompletionLabel(date: String): String
    fun summaryTimeLabel(time: String): String
    fun backupExported(items: Int, images: Int): String
    fun backupRestored(items: Int, images: Int): String
    val backupSettingsFailedSuffix: String
    fun planReminderTitle(itemName: String): String
    fun planReminderMessage(amount: String, frequency: PlanFrequency): String
    fun goalNotificationTitle(itemName: String): String
    fun goalNotificationMessage(saved: String, target: String): String
    fun summaryNotificationMessage(active: Int, saved: String, funded: Int): String
    fun planFrequency(frequency: PlanFrequency): String
    fun pageIndicator(current: Int, total: Int): String
}

object AppStringsFactory {
    fun forLanguage(language: AppLanguage): AppStrings = when (language) {
        AppLanguage.ENGLISH -> EnglishStrings
        AppLanguage.INDONESIAN -> IndonesianStrings
    }
}
