package kr.co.hs.sudoku.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
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
    @DataSourceModule.ProfileRemoteSourceQualifier
    @Provides
    @Singleton
    fun provideProfileRemoteSource(): ProfileRemoteSource = ProfileRemoteSourceImpl()
        .apply {
            rootDocument = FirebaseFirestore.getInstance()
                .collection("version")
                .document("test")
        }

    @DataSourceModule.ProfileDataSourceQualifier
    @Provides
    @Singleton
    fun bindProfileDataSource(): ProfileDataSource = ProfileDataSourceImpl()
}