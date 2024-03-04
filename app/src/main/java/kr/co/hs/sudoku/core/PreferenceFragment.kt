package kr.co.hs.sudoku.core

import androidx.preference.PreferenceFragmentCompat

abstract class PreferenceFragment : PreferenceFragmentCompat() {
    //--------------------------------------------------------------------------------------------\\
    //----------------------------------------- conv -------------------------------------------\\
    //--------------------------------------------------------------------------------------------\\

    protected val activity: Activity
        get() = super.getActivity() as Activity
}