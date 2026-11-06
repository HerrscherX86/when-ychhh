package id.my.f1zz.whenyah.data.local

import androidx.room.TypeConverter
import id.my.f1zz.whenyah.domain.model.ItemStatus
import id.my.f1zz.whenyah.domain.model.PlanFrequency
import id.my.f1zz.whenyah.domain.model.SavingsEventType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class Converters {
    @TypeConverter
    fun uuidToString(value: UUID?): String? = value?.toString()

    @TypeConverter
    fun stringToUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun instantToEpochMilli(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMilliToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun itemStatusToString(value: ItemStatus?): String? = value?.name

    @TypeConverter
    fun stringToItemStatus(value: String?): ItemStatus? = value?.let { ItemStatus.valueOf(it) }

    @TypeConverter
    fun savingsEventTypeToString(value: SavingsEventType?): String? = value?.name

    @TypeConverter
    fun stringToSavingsEventType(value: String?): SavingsEventType? =
        value?.let { SavingsEventType.valueOf(it) }

    @TypeConverter
    fun planFrequencyToString(value: PlanFrequency?): String? = value?.name

    @TypeConverter
    fun stringToPlanFrequency(value: String?): PlanFrequency? =
        value?.let { PlanFrequency.valueOf(it) }
}
