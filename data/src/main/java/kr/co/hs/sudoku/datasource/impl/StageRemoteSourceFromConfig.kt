package kr.co.hs.sudoku.datasource.impl

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import kr.co.hs.sudoku.datasource.StageRemoteSource
import kr.co.hs.sudoku.model.sudoku.impl.AutoGenStageModelImpl

class StageRemoteSourceFromConfig(
    private val remoteConfig: FirebaseRemoteConfig
) : StageRemoteSource {
    companion object {
        const val CONFIG_BEGINNER = "v2_generate_mask_beginner"
        const val CONFIG_INTERMEDIATE = "v2_generate_mask_intermediate"
        const val CONFIG_ADVANCED = "v2_generate_mask_advanced"
    }

    override suspend fun getBeginnerGenerateMask() = with(remoteConfig) {
        fetchAndActivate().await()
        getString(CONFIG_BEGINNER)
            .runCatching { Gson().fromJson(this, DataList::class.java) }
            .getOrDefault(DataList(emptyList()))
            .data
    }


    override suspend fun getIntermediateGenerateMask() = remoteConfig.getString(CONFIG_INTERMEDIATE)
        .runCatching { Gson().fromJson(this, DataList::class.java) }
        .getOrDefault(DataList(emptyList()))
        .data

    override suspend fun getAdvancedGenerateMask() = remoteConfig.getString(CONFIG_ADVANCED)
        .runCatching { Gson().fromJson(this, DataList::class.java) }
        .getOrDefault(DataList(emptyList()))
        .data

    private data class DataList(val data: List<AutoGenStageModelImpl>)
}