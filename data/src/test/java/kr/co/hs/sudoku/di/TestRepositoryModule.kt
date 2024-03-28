package kr.co.hs.sudoku.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.repository.challenge.ChallengeRepository
import kr.co.hs.sudoku.repository.challenge.ChallengeRepositoryImpl
import kr.co.hs.sudoku.repository.history.HistoryRepository
import kr.co.hs.sudoku.repository.history.HistoryRepositoryImpl
import kr.co.hs.sudoku.repository.history.MyHistoryRepository
import kr.co.hs.sudoku.repository.history.MyHistoryRepositoryImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import kr.co.hs.sudoku.repository.user.TestProfileRepository
import kr.co.hs.sudoku.usecase.NoErrorUseCase
import kr.co.hs.sudoku.usecase.challenge.GetChallengeUseCase
import java.util.Date
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun bindProfileRepository(
        profileDataSource: ProfileDataSource,
        profileRemoteSource: ProfileRemoteSource
    ): ProfileRepository = TestProfileRepository(
        profileDataSource,
        profileRemoteSource
    )

    @Provides
    @Singleton
    fun bindChallengeRepository(
        challengeRemoteSource: ChallengeRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource,
        logRemoteSource: LogRemoteSource
    ): ChallengeRepository = ChallengeRepositoryImpl(
        challengeRemoteSource,
        recordRemoteSource,
        logRemoteSource,
        getUserProfileUseCase
    )

    private val getUserProfileUseCase: NoErrorUseCase<Unit, ProfileEntity>
        get() = object : NoErrorUseCase<Unit, ProfileEntity>() {
            override fun invoke(param: Unit): Flow<Result<ProfileEntity>> = flow {
                val profile = object : ProfileEntity.UserEntity {
                    override val lastCheckedAt: Date = Date()
                    override val uid: String = "profile-uid"
                    override var displayName: String = "displayName"
                    override var message: String? = "message"
                    override var iconUrl: String? = null
                    override val locale: LocaleEntity? = null
                }
                emit(Result.Success(profile))
            }

        }

    @Provides
    @Singleton
    fun bindHistoryRepository(
        getChallengeUseCase: GetChallengeUseCase,
        logRemoteSource: LogRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource
    ): HistoryRepository = HistoryRepositoryImpl(
        getChallengeUseCase,
        logRemoteSource,
        recordRemoteSource
    )

    @Provides
    @Singleton
    fun bindMyHistoryRepository(
        getChallengeUseCase: GetChallengeUseCase,
        logRemoteSource: LogRemoteSource,
        recordRemoteSource: ChallengeRecordRemoteSource
    ): MyHistoryRepository = MyHistoryRepositoryImpl(
        getChallengeUseCase,
        logRemoteSource,
        recordRemoteSource,
        getUserProfileUseCase
    )
}