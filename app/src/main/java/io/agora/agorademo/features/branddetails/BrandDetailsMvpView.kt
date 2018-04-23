package io.agora.agorademo.features.branddetails

import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.features.base.MvpView

/**
 * Created by saiki on 22-04-2018.
 **/
interface BrandDetailsMvpView : MvpView {
    fun showProgress(show: Boolean)
    fun showError(message: String)
    fun setAdapter(adapter: LastAdapter)
    fun launchBroadcastActivity(broadcast: Broadcast, brand: Brand, role: Int)
    fun toggleEmptyView(empty: Boolean)
}