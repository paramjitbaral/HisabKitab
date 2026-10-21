package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Loan

@Database(entities = [Loan::class], version = 2, exportSchema = false)
abstract class MiniLedgerDatabase : RoomDatabase() {
    abstract fun loanDao(): LoanDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE loans ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: MiniLedgerDatabase? = null

        fun getDatabase(context: Context): MiniLedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiniLedgerDatabase::class.java,
                    "miniledger_db"
                ).addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
