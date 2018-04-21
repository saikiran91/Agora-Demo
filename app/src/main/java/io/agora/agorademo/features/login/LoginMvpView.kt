package io.agora.agorademo.features.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.agora.agorademo.features.base.MvpView


interface LoginMvpView : MvpView {
    fun showProgress(show: Boolean)
    fun showError(message: String)
    fun launchBrandsActivity()
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?)
}