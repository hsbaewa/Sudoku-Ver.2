package kr.co.hs.sudoku.di

import kr.co.hs.sudoku.repository.ProfileRepositoryImpl
import kr.co.hs.sudoku.repository.user.ProfileRepository
import java.lang.ref.WeakReference

object Repositories {
    private var weakRefProfileRepository: WeakReference<ProfileRepository>? = null
    fun getProfileRepository() = weakRefProfileRepository
        ?.get()
        ?: ProfileRepositoryImpl().also { weakRefProfileRepository = WeakReference(it) }
}