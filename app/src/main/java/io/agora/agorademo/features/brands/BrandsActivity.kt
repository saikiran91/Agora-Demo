package io.agora.agorademo.features.brands

import android.os.Bundle
import android.support.design.widget.Snackbar
import io.agora.agorademo.R
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.features.login.LoginPresenter
import kotlinx.android.synthetic.main.activity_brands.*
import javax.inject.Inject

class BrandsActivity() : MvpBaseActivity() {
    override val layout: Int = R.layout.activity_brands
    @Inject
    lateinit var mPresenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

}
