package kr.co.hs.sudoku.core

interface Timer {
    fun start(): Long
    fun getStartTime(): Long
    fun getPassedTime(): Long
    fun getTickInterval(): Long
    fun finish(): Long
    fun getFinishTime(): Long
}