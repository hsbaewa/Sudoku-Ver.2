package kr.co.hs.sudoku.repository

interface TestableRepository {
    fun setFireStoreRootVersion(versionName: String)
}