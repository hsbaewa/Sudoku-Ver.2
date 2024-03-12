package kr.co.hs.sudoku.auth

import com.google.firebase.auth.FirebaseAuth

class AuthenticatorImpl(
    private val firebaseAuth: FirebaseAuth
) : Authenticator {
    override val uid: String
        get() = firebaseAuth.currentUser?.uid?.takeIf { it.isNotEmpty() }
            ?: throw NoAuthUserException("no user info or uid is empty")
}