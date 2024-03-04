package kr.co.hs.sudoku

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.EmptyFragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.core.internal.deps.dagger.internal.Preconditions

object HiltTestUtil {

    /**
     * launchFragmentInContainer from the androidx.fragment:fragment-testing library
     * is NOT possible to use right now as it uses a hardcoded Activity under the hood
     * (i.e. [EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
     *
     * As a workaround, use this function that is equivalent. It requires you to add
     * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
     * as can be found in this project.
     */
    data class HiltFragmentScenario<T : Fragment>(
        val cls: Class<T>,
        val activityScenario: ActivityScenario<HiltTestActivity>,
        val fragmentArgs: Bundle? = null
    ) {

        inline fun onFragment(crossinline action: (T) -> Unit) {
            activityScenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager.findFragmentByTag("testFragment")
                    ?: run {
                        activity.supportFragmentManager.fragmentFactory.instantiate(
                            Preconditions.checkNotNull(cls.classLoader) as ClassLoader,
                            cls.name
                        ).also {
                            it.arguments = fragmentArgs
                            activity.supportFragmentManager.beginTransaction()
                                .add(android.R.id.content, it, "testFragment").commitNow()
                        }
                    }
                @Suppress("UNCHECKED_CAST")
                action(fragment as T)
            }
        }

        fun moveToState(state: Lifecycle.State) = activityScenario.moveToState(state)
    }


    inline fun <reified T : Fragment> launchFragmentInHiltContainer(
        fragmentArgs: Bundle? = null,
        @StyleRes themeResId: Int = R.style.Theme_HSSudoku2,
        initialState: Lifecycle.State? = null
    ): HiltFragmentScenario<T> {
        val startActivityIntent = Intent.makeMainActivity(
            ComponentName(
                ApplicationProvider.getApplicationContext<Application>(),
                HiltTestActivity::class.java
            )
        ).putExtra(EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY, themeResId)

        val activityScenario = ActivityScenario.launch<HiltTestActivity>(startActivityIntent)
        initialState?.let { activityScenario.moveToState(it) }
        return HiltFragmentScenario(T::class.java, activityScenario, fragmentArgs)
    }

}