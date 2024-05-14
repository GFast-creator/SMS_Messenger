package ru.gfastg98.sms_messenger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.gfastg98.sms_messenger.room.AppDatabase
import ru.gfastg98.sms_messenger.room.MessageDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {
    const val NOTIFICATION_ID = 1990
    const val CHANNEL_ID = "1991"
    const val CHANNEL_NAME = "list_app"

    @Singleton
    @Provides
    fun create(@ApplicationContext context : Context) : MessageDao {
        return AppDatabase.create(context).messengerDao()
    }

    @Singleton
    @Provides
    fun vibrator(context: Application): Vibrator {
        return (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
            .defaultVibrator

    }

    @Singleton
    @Provides
    fun notificationManager(app: Application): NotificationManager {
        return (app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "channel for app"
                }
            )
        }
    }
}