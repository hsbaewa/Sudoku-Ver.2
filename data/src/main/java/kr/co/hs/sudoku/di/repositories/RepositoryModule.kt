package kr.co.hs.sudoku.di.repositories

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.datasource.admin.AdminRemoteSource
import kr.co.hs.sudoku.datasource.battle.BattleRemoteSource
import kr.co.hs.sudoku.datasource.user.ProfileRemoteSource
import kr.co.hs.sudoku.di.network.AdminRemoteSourceQualifier
import kr.co.hs.sudoku.di.network.BattleRemoteSourceQualifier
import kr.co.hs.sudoku.di.network.StageRemoteSourceQualifier
import kr.co.hs.sudoku.model.matrix.AdvancedMatrix
import kr.co.hs.sudoku.model.matrix.BeginnerMatrix
import kr.co.hs.sudoku.model.matrix.IntermediateMatrix
import kr.co.hs.sudoku.repository.AdvancedMatrixRepository
import kr.co.hs.sudoku.repository.BeginnerMatrixRepository
import kr.co.hs.sudoku.repository.GameSettingsRepositoryImpl
import kr.co.hs.sudoku.repository.IntermediateMatrixRepository
import kr.co.hs.sudoku.repository.RegistrationRepositoryImpl
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepository
import kr.co.hs.sudoku.repository.admin.AdminPermissionRepositoryImpl
import kr.co.hs.sudoku.repository.battle.BattleRepository
import kr.co.hs.sudoku.repository.battle.BattleRepositoryImpl
import kr.co.hs.sudoku.repository.settings.GameSettingsRepository
import kr.co.hs.sudoku.repository.settings.RegistrationRepository
import kr.co.hs.sudoku.repository.stage.MatrixRepository
import kr.co.hs.sudoku.usecase.SudokuGenerateUseCase
import kr.co.hs.sudoku.usecase.history.CreateHistoryUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @AdminRepositoryQualifier
    @Singleton
    @Provides
    fun provideAdminRepository(
        @AdminRemoteSourceQualifier adminRemoteSource: AdminRemoteSource
    ): AdminPermissionRepository = AdminPermissionRepositoryImpl(adminRemoteSource)

    @BattleRepositoryQualifier
    @Singleton
    @Provides
    fun provideBattleRepository(
        @BattleRemoteSourceQualifier battleRemoteSource: BattleRemoteSource,
        profileRemoteSource: ProfileRemoteSource,
        sudokuGenerate: SudokuGenerateUseCase,
        createHistory: CreateHistoryUseCase
    ): BattleRepository =
        BattleRepositoryImpl(
            battleRemoteSource,
            profileRemoteSource,
            sudokuGenerate,
            createHistory
        )

    @BeginnerMatrixRepositoryQualifier
    @Singleton
    @Provides
    fun provideBeginnerMatrixRepository(
        @StageRemoteSourceQualifier stageRemoteSource: StageRemoteSource
    ): MatrixRepository<BeginnerMatrix> = BeginnerMatrixRepository(stageRemoteSource)

    @IntermediateMatrixRepositoryQualifier
    @Singleton
    @Provides
    fun provideIntermediateMatrixRepository(
        @StageRemoteSourceQualifier stageRemoteSource: StageRemoteSource
    ): MatrixRepository<IntermediateMatrix> = IntermediateMatrixRepository(stageRemoteSource)

    @AdvancedMatrixRepositoryQualifier
    @Singleton
    @Provides
    fun provideAdvancedMatrixRepository(
        @StageRemoteSourceQualifier stageRemoteSource: StageRemoteSource
    ): MatrixRepository<AdvancedMatrix> = AdvancedMatrixRepository(stageRemoteSource)


    private const val USER_PREFERENCES_NAME = "sudoku.preferences"
    private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

    @GameSettingsRepositoryQualifier
    @Singleton
    @Provides
    fun provideGameSettingsRepository(
        @ApplicationContext context: Context
    ): GameSettingsRepository = GameSettingsRepositoryImpl(context.dataStore)

    @RegistrationRepositoryQualifier
    @Singleton
    @Provides
    fun provideRegistrationRepository(
        @ApplicationContext context: Context
    ): RegistrationRepository = RegistrationRepositoryImpl(context.dataStore)
}