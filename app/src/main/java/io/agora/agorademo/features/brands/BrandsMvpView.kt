package io.agora.agorademo.features.brands

import android.support.v4.util.Pair
import android.view.View
import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.features.base.MvpView

/**
 * Created by saiki on 22-04-2018.
 **/
interface BrandsMvpView : MvpView {

    fun setAdapter(adapter: LastAdapter)
    fun showProgress(show: Boolean)
    fun showError(message: String)
    fun launchBrandDetails(pair: Pair<View, String>?, brand: Brand)
}