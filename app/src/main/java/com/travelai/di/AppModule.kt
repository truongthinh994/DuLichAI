package com.travelai.di

import android.content.Context
import androidx.room.Room
import com.travelai.BuildConfig
import com.travelai.data.api.ApiClient
import com.travelai.data.api.DeepSeekApi
import com.travelai.data.db.AppDatabase
import com.travelai.data.db.ChatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("DeepSeekApiKey")
    fun provideDeepSeekApiKey(): String = BuildConfig.DEEPSEEK_API_KEY

    @Provides
    @Singleton
    fun provideDeepSeekApi(
        @Named("DeepSeekApiKey") apiKey: String
    ): DeepSeekApi = ApiClient.create(apiKey)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "travelai.db"
    ).build()

    @Provides
    @Singleton
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()
}
