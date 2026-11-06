package id.my.f1zz.whenyah

import id.my.f1zz.whenyah.data.backup.BackupCollection
import id.my.f1zz.whenyah.data.backup.BackupData
import id.my.f1zz.whenyah.data.backup.BackupItem
import id.my.f1zz.whenyah.data.backup.BackupPurchaseRecord
import id.my.f1zz.whenyah.data.backup.BackupSavingsEvent
import id.my.f1zz.whenyah.data.backup.BackupSavingsPlan
import id.my.f1zz.whenyah.data.backup.BackupSettings
import id.my.f1zz.whenyah.data.backup.BackupValidationException
import id.my.f1zz.whenyah.data.backup.BackupValidator
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import id.my.f1zz.whenyah.domain.settings.AccentColor
import id.my.f1zz.whenyah.domain.settings.SummaryCadence
import id.my.f1zz.whenyah.domain.settings.ThemeMode
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime
import java.util.UUID

class BackupValidatorTest {
    private val collectionId = UUID.fromString("11111111-1111-1111-1111-111111111111")
    private val itemId = UUID.fromString("22222222-2222-2222-2222-222222222222")
    private val eventId = UUID.fromString("33333333-3333-3333-3333-333333333333")
    private val planId = UUID.fromString("44444444-4444-4444-4444-444444444444")

    @Test
    fun validBackupMapsToEntitiesAndSettings() {
        val validated = BackupValidator.validate(validData())

        assertEquals(1, validated.collections.size)
        assertEquals(1, validated.items.size)
        assertEquals(1, validated.events.size)
        assertEquals(1, validated.plans.size)
        assertEquals(ThemeMode.SYSTEM, validated.settings.themeMode)
        assertEquals(AccentColor.BLUE, validated.settings.accentColor)
        assertEquals(SummaryCadence.WEEKLY, validated.settings.summaryCadence)
        assertEquals(LocalTime.of(8, 30), validated.settings.summaryTime)
        assertEquals(PlanFrequency.WEEKLY, validated.plans.single().frequency)
        assertEquals(SavingsEventType.DEPOSIT, validated.events.single().type)
    }

    @Test
    fun jsonRoundTripStillValidates() {
        val json = Json {
            ignoreUnknownKeys = false
            encodeDefaults = true
        }

        val encoded = json.encodeToString(validData())
        val decoded = json.decodeFromString<BackupData>(encoded)
        val validated = BackupValidator.validate(decoded)

        assertEquals(1, validated.items.size)
        assertEquals("Gamesir Super Nova", validated.items.single().name)
    }

    @Test
    fun itemCannotReferenceMissingCollection() {
        assertInvalid(
            validData().copy(
                items = listOf(
                    validData().items.single().copy(
                        collectionId = UUID.randomUUID().toString(),
                    )
                )
            )
        )
    }

    @Test
    fun negativeItemTargetIsRejected() {
        assertInvalid(
            validData().copy(
                items = listOf(validData().items.single().copy(targetPriceIdr = -1))
            )
        )
    }

    @Test
    fun nonPositiveEventAmountIsRejected() {
        assertInvalid(
            validData().copy(
                savingsEvents = listOf(
                    validData().savingsEvents.single().copy(amountIdr = 0)
                )
            )
        )
    }

    @Test
    fun eventMustReferenceExistingItem() {
        assertInvalid(
            validData().copy(
                savingsEvents = listOf(
                    validData().savingsEvents.single().copy(
                        itemId = UUID.randomUUID().toString(),
                    )
                )
            )
        )
    }

    @Test
    fun purchaseSpendMustReferenceExistingPurchaseRecord() {
        assertInvalid(
            validData().copy(
                savingsEvents = listOf(
                    validData().savingsEvents.single().copy(
                        type = SavingsEventType.PURCHASE_SPEND.name,
                        purchaseRecordId = UUID.randomUUID().toString(),
                    )
                )
            )
        )
    }

    @Test
    fun duplicatePlanForItemIsRejected() {
        val plan = validData().savingsPlans.single()
        assertInvalid(
            validData().copy(
                savingsPlans = listOf(
                    plan,
                    plan.copy(id = UUID.randomUUID().toString()),
                )
            )
        )
    }

    @Test
    fun enabledReminderRequiresTime() {
        assertInvalid(
            validData().copy(
                savingsPlans = listOf(
                    validData().savingsPlans.single().copy(
                        reminderEnabled = true,
                        reminderLocalTime = null,
                    )
                )
            )
        )
    }

    @Test
    fun invalidEnumIsRejected() {
        assertInvalid(
            validData().copy(
                items = listOf(validData().items.single().copy(status = "ARCHIVED"))
            )
        )
    }

    @Test
    fun unsafeImagePathIsRejected() {
        assertInvalid(
            validData().copy(
                items = listOf(
                    validData().items.single().copy(
                        imageRelativePath = "../outside.jpg",
                    )
                )
            )
        )
    }

    @Test
    fun invalidSettingsAreRejected() {
        assertInvalid(
            validData().copy(
                settings = validData().settings.copy(summaryTime = "not-a-time")
            )
        )
    }

    @Test
    fun missingPurchaseRecordReferenceIsRejected() {
        assertInvalid(
            validData().copy(
                savingsEvents = listOf(
                    validData().savingsEvents.single().copy(
                        type = SavingsEventType.PURCHASE_SPEND.name,
                        purchaseRecordId = UUID.randomUUID().toString(),
                    )
                ),
                purchaseRecords = listOf(
                    BackupPurchaseRecord(
                        id = UUID.randomUUID().toString(),
                        itemId = itemId.toString(),
                        completedAtEpochMillis = 1_700_000_000_000,
                        savingsSpentIdr = 100,
                    )
                ),
            )
        )
    }

    @Test
    fun emptyBackupIsValid() {
        val validated = BackupValidator.validate(
            validData().copy(
                collections = emptyList(),
                items = emptyList(),
                savingsEvents = emptyList(),
                savingsPlans = emptyList(),
                purchaseRecords = emptyList(),
            )
        )

        assertTrue(validated.collections.isEmpty())
        assertTrue(validated.items.isEmpty())
    }

    private fun assertInvalid(data: BackupData) {
        assertThrows(BackupValidationException::class.java) {
            BackupValidator.validate(data)
        }
    }

    private fun validData(): BackupData = BackupData(
        collections = listOf(
            BackupCollection(
                id = collectionId.toString(),
                name = "Gaming",
                createdAtEpochMillis = 1_700_000_000_000,
                updatedAtEpochMillis = 1_700_000_100_000,
            )
        ),
        items = listOf(
            BackupItem(
                id = itemId.toString(),
                name = "Gamesir Super Nova",
                targetPriceIdr = 515_000,
                imageRelativePath = "item_images/controller.jpg",
                purchaseUrl = "https://example.com/controller",
                notes = "Wireless controller",
                category = "Gaming",
                collectionId = collectionId.toString(),
                status = "ACTIVE",
                createdAtEpochMillis = 1_700_000_000_000,
                updatedAtEpochMillis = 1_700_000_100_000,
                purchasedAtEpochMillis = null,
            )
        ),
        savingsEvents = listOf(
            BackupSavingsEvent(
                id = eventId.toString(),
                itemId = itemId.toString(),
                type = "DEPOSIT",
                amountIdr = 100_000,
                note = "First deposit",
                createdAtEpochMillis = 1_700_000_200_000,
                purchaseRecordId = null,
            )
        ),
        savingsPlans = listOf(
            BackupSavingsPlan(
                id = planId.toString(),
                itemId = itemId.toString(),
                contributionIdr = 100_000,
                frequency = "WEEKLY",
                reminderEnabled = true,
                reminderLocalTime = "09:00",
                active = true,
                createdAtEpochMillis = 1_700_000_000_000,
                updatedAtEpochMillis = 1_700_000_100_000,
            )
        ),
        purchaseRecords = emptyList(),
        settings = BackupSettings(
            themeMode = "SYSTEM",
            accentColor = "BLUE",
            useDynamicColor = false,
            notificationsEnabled = true,
            goalNotificationsEnabled = true,
            summaryCadence = "WEEKLY",
            summaryTime = "08:30",
        ),
    )
}
