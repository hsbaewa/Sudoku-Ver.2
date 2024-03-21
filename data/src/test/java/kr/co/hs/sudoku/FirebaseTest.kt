package kr.co.hs.sudoku

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Before
import org.robolectric.RuntimeEnvironment

abstract class FirebaseTest {
    @Before
    open fun onBefore() {
        runCatching {
            FirebaseApp.getInstance()
        }.getOrElse {
            FirebaseApp.initializeApp(
                RuntimeEnvironment.getApplication(),
                FirebaseOptions.Builder()
                    .setProjectId("sudokubattle-0")
                    .setApplicationId("1:616519348076:android:7e79f73996ffc659234d49")
                    .build()
            )
        }
    }
}