package io.agora.agorademo.features.branddetails

import io.agora.agorademo.features.base.MvpView

/**
 * Created by saiki on 22-04-2018.
 **/
interface BrandDetailsMvpView:MvpView{
    fun showProgress(show: Boolean)
    fun showError(message: String)
}