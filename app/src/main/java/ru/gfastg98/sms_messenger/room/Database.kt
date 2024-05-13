package ru.gfastg98.sms_messenger.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Message::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messengerDao(): MessageDao

    companion object{
        private var instance: AppDatabase? = null

        fun create(context : Context) : AppDatabase {
            if (instance == null){
                instance = Room
                    .databaseBuilder(context, AppDatabase::class.java, "database.db")
                    .addTypeConverter(Converters())
                    .build()
            }
            return instance as AppDatabase
        }
    }
}


