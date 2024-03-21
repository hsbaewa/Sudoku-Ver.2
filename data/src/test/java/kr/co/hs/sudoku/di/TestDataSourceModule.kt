package kr.co.hs.sudoku.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kr.co.hs.sudoku.datasource.challenge.ChallengeRecordRemoteSource
import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRecordRemoteSourceImpl
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl
import kr.co.hs.sudoku.datasource.logs.LogRemoteSource
import kr.co.hs.sudoku.datasource.logs.impl.LogRemoteSourceImpl
import kr.co.hs.sudoku.datasource.user.ProfileDataSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.datasource.user.impl.ProfileDataSourceImpl
import kr.co.hs.sudoku.datasource.user.impl.ProfileRemoteSourceImpl
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataSourceModule::class]
)
object TestDataSourceModule {

    private val testRootDocument = FirebaseFirestore.getInstance()
        .collection("version")
        .document("test")

    @Provides
    @Singleton
    fun provideProfileRemoteSource(): ProfileRemoteSource = ProfileRemoteSourceImpl()
        .apply { rootDocument = testRootDocument }

    @Provides
    @Singleton
    fun bindProfileDataSource(): ProfileDataSource = ProfileDataSourceImpl()

    @Provides
    @Singleton
    fun bindLogRemoteSource(): LogRemoteSource = LogRemoteSourceImpl()
        .apply { rootDocument = testRootDocument }

    @Provides
    @Singleton
    fun bindChallengeRemoteSource(): ChallengeRemoteSource = ChallengeRemoteSourceImpl()
        .apply { rootDocument = testRootDocument }

    @Provides
    @Singleton
    fun bindChallengeRecordRemoteSource(
        logRemoteSource: LogRemoteSource
    ): ChallengeRecordRemoteSource = ChallengeRecordRemoteSourceImpl(logRemoteSource)
        .apply { rootDocument = testRootDocument }
}