package io.agora.agorademo.features.login

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import io.agora.agorademo.R
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.features.brands.BrandsActivity
import io.agora.agorademo.features.common.ErrorView
import io.agora.agorademo.util.showAsToast
import io.agora.agorademo.util.visible
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject


class LoginActivity : MvpBaseActivity(), LoginMvpView, ErrorView.ErrorListener {

    @Inject
    lateinit var mMainPresenter: LoginPresenter
    private val gso: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
    }
    private val mGoogleSignInClient by lazy { GoogleSignIn.getClient(this, gso) }
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    override val layout: Int get() = R.layout.activity_login
    override fun showProgress(show: Boolean) = progressbar.visible(show)
    override fun showError(message: String) = message.showAsToast()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        mMainPresenter.attachView(this)
        login_btn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        checkUserLogin()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /**
     * Check if the users is already logged in
     */
    private fun checkUserLogin() {
        if (mAuth.currentUser != null) {
            launchBrandsActivity()
        }
    }

    override fun launchBrandsActivity() {
        val intent = Intent(this, BrandsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onReloadData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mMainPresenter.detachView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mMainPresenter.validateActivityResult(requestCode, data)
    }

    override fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        Timber.d("firebaseAuthWithGoogle:%s", account?.id!!)

        showProgress(true)


        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Timber.d("signInWithCredential:success")
                        val user = mAuth.currentUser
                        mMainPresenter.saveUserDetails(user)
                        launchBrandsActivity()
                    } else {
                        // If sign in fails, display a message to the user.
                        Timber.w(task.exception, "signInWithCredential:failure")
                        Snackbar.make(findViewById(R.id.parent), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        showError("Google sign in failed.")
                    }

                    showProgress(false)
                })
    }

    companion object {
        const val RC_SIGN_IN = 9001
    }
}
