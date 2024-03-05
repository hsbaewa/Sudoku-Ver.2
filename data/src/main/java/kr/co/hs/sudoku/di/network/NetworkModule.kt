package kr.co.hs.sudoku.di.network

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.datasource.admin.AdminRemoteSource
import kr.co.hs.sudoku.datasource.admin.impl.AdminRemoteSourceImpl
import kr.co.hs.sudoku.datasource.battle.BattleRemoteSource
import kr.co.hs.sudoku.datasource.battle.impl.BattleRemoteSourceImpl
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.impl.StageRemoteSourceFromConfig
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.logs.impl.LogRemoteSourceImpl
import kr.co.hs.sudoku.datasource.record.RecordRemoteSource
import kr.co.hs.sudoku.datasource.record.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * @Binds, @Provides : inject가 기본 생성자로만 제공되는 경우는 Binds(abstract class) 생성자 파라미터가 필요한 경우는 Provides(object)
     * @Qualifier : 한 모듈에서 리턴 타입이 동일한 경우(interface 실체화가 두개 이상) Qualifier annotation을 구현하여 구분
     */

    @AdminRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideAdminRemoteSource(): AdminRemoteSource = AdminRemoteSourceImpl()

    @BattleRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideBattleRemoteSource(): BattleRemoteSource = BattleRemoteSourceImpl()

    @ChallengeRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideChallengeRemoteSource(): ChallengeRemoteSource = ChallengeRemoteSourceImpl()

    @StageRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideStageRemoteSource(): StageRemoteSource =
        StageRemoteSourceFromConfig(FirebaseRemoteConfig.getInstance())

    @LogRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideLogRemoteSource(): LogRemoteSource = LogRemoteSourceImpl()

    @ProfileRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideProfileRemoteSource(): ProfileRemoteSource = ProfileRemoteSourceImpl()

    @ChallengeRecordRemoteSourceQualifier
    @Singleton
    @Provides
    fun provideRecordRemoteSource(): RecordRemoteSource = ChallengeRecordRemoteSourceImpl()
}