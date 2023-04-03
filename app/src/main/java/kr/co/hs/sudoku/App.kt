package kr.co.hs.sudoku

import android.app.Application
import com.google.android.gms.games.PlayGamesSdk

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this)
    }
}