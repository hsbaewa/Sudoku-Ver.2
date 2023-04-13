package kr.co.hs.sudoku.model.gamelog

import kotlinx.coroutines.flow.Flow

interface CellLogStreamEntity {
    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 지나간 시간 만큼 로그 이벤트 방출
     * @param passedTime : 흘러간 시간
     **/
    suspend fun pop(passedTime: Long)

    /**
     * @author hsbaewa@gmail.com
     * @since 2023/04/13
     * @comment 로그 이벤트 flow(hot stream <- SharedFlow)
     * @return flow
     **/
    fun getLogStream(): Flow<CellLogEntity>
}