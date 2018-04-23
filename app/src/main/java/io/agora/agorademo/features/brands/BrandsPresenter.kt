package io.agora.agorademo.features.brands

import android.databinding.ObservableArrayList
import android.os.Build
import android.support.v4.util.Pair
import android.view.View
import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.BR
import io.agora.agorademo.R
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.databinding.ItemBrandsBinding
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import io.agora.agorademo.util.clearAndAddAll
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_brands.view.*
import javax.inject.Inject

/**
 * Created by saiki on 22-04-2018.
 **/
@ConfigPersistent
class BrandsPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<BrandsMvpView>() {
    private val listOfBands = ObservableArrayList<Brand>()
    private val lastAdapter: LastAdapter by lazy {
        LastAdapter(listOfBands, BR.brand)
                .map<Brand, ItemBrandsBinding>(R.layout.item_brands) {
                    onClick {
                        val p1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Pair(it.itemView.brand_image_iv as View, it.itemView.brand_image_iv.transitionName)
                        } else null

                        mvpView?.launchBrandDetails(p1, it.binding.brand!!)
                    }
                }
    }

    override fun attachView(mvpView: BrandsMvpView) {
        super.attachView(mvpView)
        mvpView.setAdapter(lastAdapter)
        loadBrands()
    }

    private fun loadBrands() {
        mvpView?.showProgress(true)
        addDisposable(mDataManager.listOfBrands()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    listOfBands.clearAndAddAll(result.sortedBy { it.order })
                    mvpView?.showProgress(false)
                },
                        { error ->
                            error.printStackTrace()
                            mvpView?.showError(error.message ?: "No message")
                            mvpView?.showProgress(false)
                        })
        )
    }


}