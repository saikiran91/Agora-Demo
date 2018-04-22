package io.agora.agorademo.features.brands

import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.features.base.MvpView

/**
 * Created by saiki on 22-04-2018.
 **/
 interface BrandsMvpView:MvpView{

    fun setAdapter(adapter: LastAdapter)
    fun showProgress(show: Boolean)
    fun showError(message: String)
}