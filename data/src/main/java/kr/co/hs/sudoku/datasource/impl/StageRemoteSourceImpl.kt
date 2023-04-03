package kr.co.hs.sudoku.datasource.impl

import com.google.gson.Gson
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.model.sudoku.impl.AutoGenStageModelImpl

class StageRemoteSourceImpl(
    private val beginnerSource: String,
    private val intermediateSource: String,
    private val advancedSource: String
) : StageRemoteSource {
    override suspend fun getBeginnerGenerateMask() =
        Gson().fromJson(beginnerSource, DataList::class.java).data

    override suspend fun getIntermediateGenerateMask() =
        Gson().fromJson(intermediateSource, DataList::class.java).data

    override suspend fun getAdvancedGenerateMask() =
        Gson().fromJson(advancedSource, DataList::class.java).data

    private data class DataList(val data: List<AutoGenStageModelImpl>)
}