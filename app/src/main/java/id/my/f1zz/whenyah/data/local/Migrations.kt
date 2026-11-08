package id.my.f1zz.whenyah.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE savings_plans ADD COLUMN reminderAnchorDate TEXT"
        )
        // Existing enabled reminders did not store the day they were enabled.
        // Use the last modification date as the closest available approximation.
        db.execSQL(
            """
            UPDATE savings_plans
            SET reminderAnchorDate = date(updatedAt / 1000, 'unixepoch', 'localtime')
            WHERE reminderEnabled = 1 AND reminderAnchorDate IS NULL
            """.trimIndent()
        )
    }
}
