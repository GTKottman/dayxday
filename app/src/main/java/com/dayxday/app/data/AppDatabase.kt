package com.dayxday.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromValueKind(value: ValueKind): String = value.name

    @TypeConverter
    fun toValueKind(value: String): ValueKind = ValueKind.valueOf(value)
}

@Database(
    entities = [DataType::class, DataEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun dataEntryDao(): DataEntryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dayxday.db"
                ).build().also { instance = it }
            }
        }
    }
}
