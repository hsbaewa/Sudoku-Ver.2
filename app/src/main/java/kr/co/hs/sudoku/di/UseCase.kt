package kr.co.hs.sudoku.di

import kr.co.hs.sudoku.di.Repositories.getChallengeRankingRepository
import kr.co.hs.sudoku.di.Repositories.getProfileRepository
import kr.co.hs.sudoku.model.user.ProfileEntity
import kr.co.hs.sudoku.usecase.ranking.GetRankingUseCaseImpl
import kr.co.hs.sudoku.usecase.user.GetProfileUseCase
import kr.co.hs.sudoku.usecase.user.SetProfileUseCase

object UseCase {

    fun setProfile(profileEntity: ProfileEntity) =
        SetProfileUseCase(getProfileRepository()).invoke(profileEntity)

    fun getProfile(uid: String) =
        GetProfileUseCase(getProfileRepository()).invoke(uid)

    fun getChallengeRanking() = GetRankingUseCaseImpl(getChallengeRankingRepository()).invoke()
}