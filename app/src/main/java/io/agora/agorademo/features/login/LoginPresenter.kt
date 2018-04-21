package io.agora.agorademo.features.login

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.local.UserPrefs
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import timber.log.Timber
import javax.inject.Inject

@ConfigPersistent
class LoginPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<LoginMvpView>() {


    fun validateActivityResult(requestCode: Int, data: Intent?) {
        if (requestCode == LoginActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                mvpView?.firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w("Google sign in failed", e)
                mvpView?.showError("Google sign in failed")
            }
        }
    }

    fun saveUserDetails(user: FirebaseUser?) {
        UserPrefs.id = user?.uid ?: ""
        UserPrefs.name = user?.displayName ?: ""
        UserPrefs.email = user?.email ?: ""
        UserPrefs.photoUrl = user?.photoUrl.toString()
    }

}