package kr.co.hs.sudoku.feature.user

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kr.co.hs.sudoku.data.TestProfileDataSource
import kr.co.hs.sudoku.model.user.LocaleEntity
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.model.user.impl.LocaleEntityImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import java.util.Date
import javax.inject.Inject

class TestAuthenticator
@Inject constructor(
    private val dataSource: TestProfileDataSource,
    private val repository: ProfileRepository
) : Authenticator {

    override fun signIn(): Flow<ProfileEntity> = flow {
        val newProfile = object : ProfileEntity.UserEntity {
            override val lastCheckedAt: Date = Date()
            override val uid: String = "new-user"
            override var displayName: String = "new-user name"
            override var message: String? = "new-user message"
            override var iconUrl: String? = null
            override val locale: LocaleEntity = LocaleEntityImpl("ko", "kr")
        }
        dataSource.dummyData[newProfile.uid] = newProfile
        emit(newProfile)
    }

    override fun getProfile(): Flow<ProfileEntity> = callbackFlow {
        repository.runCatching {
            send(getProfile("new-user"))
            close()
        }.getOrElse { onException(it) }

        awaitClose()
    }

    private fun ProducerScope<ProfileEntity>.onException(t: Throwable) = when (t) {
        is ProfileRepository.ProfileException -> when (t) {
            is ProfileRepository.ProfileException.EmptyUserId -> close(t)
            is ProfileRepository.ProfileException.ProfileNotFound ->
                close(Authenticator.RequireSignIn("unknown user"))
        }

        else -> close(t)
    }

    override fun checkIn(): Flow<ProfileEntity> = callbackFlow {
        repository.runCatching {
            send(checkIn("new-user"))
            close()
        }.getOrElse { onException(it) }

        awaitClose()
    }

    override fun checkOut(): Flow<ProfileEntity> = callbackFlow {
        repository.runCatching {
            send(checkOut("new-user"))
            close()
        }.getOrElse { onException(it) }

        awaitClose()
    }

    override fun updateProfile(profileEntity: ProfileEntity): Flow<ProfileEntity> = callbackFlow {
        repository.runCatching {
            setProfile(profileEntity)
            send(profileEntity)
        }.getOrElse { onException(it) }

        awaitClose()
    }
}