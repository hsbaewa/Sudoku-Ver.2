package kr.co.hs.sudoku

import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class FirebaseGamesAuthTest {
    @Test
    fun useAppContext() = runBlocking {
        // Context of the app under test.
        val scenario = ActivityScenario.launch(AuthTestActivity::class.java)

        delay(5000)

        scenario.onActivity { activity ->
            val text = activity.findViewById<TextView>(R.id.tvInfo).text.toString()
            assertEquals("제르체프", text)
        }

        return@runBlocking

    }
}