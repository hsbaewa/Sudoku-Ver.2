package kr.co.hs.sudoku.mapper

import kotlin.reflect.full.memberProperties

object Mapper {
    inline fun <reified T : Any> T.asMap() =
        with(T::class.memberProperties.associateBy { it.name }) {
            keys.associateWith { this[it]?.get(this@asMap) }
        }

    inline fun <reified T : Any> T.asMutableMap() = asMap().toMutableMap().apply {
        values.removeIf { it == null }
    }
}