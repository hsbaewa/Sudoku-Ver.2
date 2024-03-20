package kr.co.hs.sudoku

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
abstract class FirebaseTest {
    @Before
    fun onBeforeFirebaseTest() {
        FirebaseApp.initializeApp(
            RuntimeEnvironment.getApplication(),
            FirebaseOptions.Builder()
                .setProjectId("")
                .setApplicationId("")
                .build()
        )
    }

    @After
    fun onAfterFirebaseTest() {
        FirebaseApp.clearInstancesForTest()
    }
}