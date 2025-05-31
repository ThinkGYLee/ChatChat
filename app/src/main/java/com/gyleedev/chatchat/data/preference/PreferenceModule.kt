package com.gyleedev.chatchat.data.preference

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {

    @Singleton
    @Provides
    fun providesMyDataPreference(@ApplicationContext context: Context): MyDataPreference {
        return MyDataPreferenceImpl(context)
    }

    @Singleton
    @Provides
    fun providesThemePreference(@ApplicationContext context: Context): ThemePreference {
        return ThemePreferenceImpl(context)
    }
}
