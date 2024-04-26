package ru.gfastg98.sms_messenger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {
    @Singleton
    @Provides
    fun create(@ApplicationContext context : Context) : MessageDao {
        return AppDatabase.create(context).messengerDao()
    }
}