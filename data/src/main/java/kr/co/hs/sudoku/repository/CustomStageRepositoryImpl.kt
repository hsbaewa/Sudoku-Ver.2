package kr.co.hs.sudoku.repository

import kr.co.hs.sudoku.mapper.StageMapper.toDomain
import kr.co.hs.sudoku.model.stage.StageBuilder
import kr.co.hs.sudoku.model.sudoku.StageModel
import kr.co.hs.sudoku.repository.stage.CustomStageRepository

class CustomStageRepositoryImpl : CustomStageRepository {

    private lateinit var stage: StageBuilder

    fun initialize(customStageModel: StageModel.CustomStageModel) {
        stage = customStageModel.toDomain()
    }

    override fun getStage() = stage
}