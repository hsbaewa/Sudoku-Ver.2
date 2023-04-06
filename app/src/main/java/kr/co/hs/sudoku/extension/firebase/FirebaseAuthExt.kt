package kr.co.hs.sudoku.extension.firebase

import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthExt {
    fun Fragment.getCurrentUser() = FirebaseAuth.getInstance().currentUser
}