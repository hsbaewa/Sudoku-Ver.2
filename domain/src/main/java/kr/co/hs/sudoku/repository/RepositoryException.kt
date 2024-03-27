package kr.co.hs.sudoku.repository

sealed class RepositoryException(p0: String?) : Exception(p0) {
    class NotFoundException(p0: String?) : RepositoryException(p0)
    class CorruptedException(p0: String?) : RepositoryException(p0)
    class EmptyIdException(p0: String?) : RepositoryException(p0)
    class InvalidateParameterException(p0: String?) : RepositoryException(p0)
}