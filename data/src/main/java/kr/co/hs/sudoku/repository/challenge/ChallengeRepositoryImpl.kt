package kr.co.hs.sudoku.repository.challenge

import kr.co.hs.sudoku.datasource.challenge.ChallengeRemoteSource
import kr.co.hs.sudoku.datasource.challenge.impl.ChallengeRemoteSourceImpl

class ChallengeRepositoryImpl(
    private val remoteSource: ChallengeRemoteSource = ChallengeRemoteSourceImpl()
) : ChallengeRepository,
    ChallengeReaderRepository by ChallengeReaderRepositoryImpl(remoteSource),
    ChallengeWriterRepository by ChallengeWriterRepositoryImpl(remoteSource)