package kr.co.hs.sudoku.di.ad

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.BuildConfig
import kr.co.hs.sudoku.extension.platform.ContextExtension.getMetaData
import kr.co.hs.sudoku.feature.ad.NativeItemAdManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdManagerModule {

    @ChallengeDashboardAdQualifier
    @Singleton
    @Provides
    fun provideChallengeDashboardAdManager(@ApplicationContext context: Context): NativeItemAdManager {
        val metaDataKey = "kr.co.hs.sudoku.adUnitId.NativeAdForChallengeItem"
        return NativeItemAdManager(
            context = context,
            adUnitId = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                context.getMetaData(metaDataKey)?.takeIf { it.isNotEmpty() }
            }
        )
    }

    @ExitAdQualifier
    @Singleton
    @Provides
    fun provideExitAdManager(@ApplicationContext context: Context): NativeItemAdManager {
        val metaDataKey = "kr.co.hs.sudoku.adUnitId.NativeAdExitPopup"
        return NativeItemAdManager(
            context = context,
            adUnitId = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                context.getMetaData(metaDataKey)?.takeIf { it.isNotEmpty() }
            }
        )
    }

    @MultiDashboardAdQualifier
    @Singleton
    @Provides
    fun provideMultiDashboardAdQualifier(@ApplicationContext context: Context): NativeItemAdManager {
        val metaDataKey = "kr.co.hs.sudoku.adUnitId.NativeAd"
        return NativeItemAdManager(
            context = context,
            adUnitId = if (BuildConfig.DEBUG) {
                "ca-app-pub-3940256099942544/2247696110"
            } else {
                context.getMetaData(metaDataKey)?.takeIf { it.isNotEmpty() }
            }
        )
    }
}