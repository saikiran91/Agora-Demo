package io.agora.agorademo.features.branddetails

import android.os.Bundle
import io.agora.agorademo.R
import io.agora.agorademo.features.base.MvpBaseActivity
import javax.inject.Inject

class BrandDetailsActivity : MvpBaseActivity(), BrandDetailsMvpView {
    @Inject
    lateinit var mPresenter: BrandDetailsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        mPresenter.attachView(this)
    }

    override val layout: Int get() = R.layout.activity_brand_details

    override fun showProgress(show: Boolean) {

    }

    override fun showError(message: String) {

    }

}
