package io.agora.agorademo.features.brands

import android.content.Intent
import android.databinding.BindingAdapter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.view.View
import android.widget.ImageView
import com.github.nitrico.lastadapter.LastAdapter
import com.squareup.picasso.Picasso
import io.agora.agorademo.R
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.features.branddetails.BrandDetailsActivity
import io.agora.agorademo.util.visible
import kotlinx.android.synthetic.main.activity_brands.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class BrandsActivity : MvpBaseActivity(), BrandsMvpView {
    @Inject
    lateinit var mPresenter: BrandsPresenter

    override val layout: Int = R.layout.activity_brands

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        mPresenter.attachView(this)
        setSupportActionBar(toolbar)
    }

    override fun showProgress(show: Boolean) {
        progressbar.visible(show)
    }

    override fun showError(message: String) {
        Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun setAdapter(adapter: LastAdapter) {
        list_view.adapter = adapter
    }

    override fun launchBrandDetails(pair: Pair<View, String>?, brand: Brand) {
        EventBus.getDefault().postSticky(BrandDetailsActivity.LaunchEvent(brand))
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair)
        startActivity(Intent(this, BrandDetailsActivity::class.java), options.toBundle())
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    companion object {
        //https://stackoverflow.com/questions/40085724/kotlin-custom-attribute-databinding
        @JvmStatic
        @BindingAdapter("bind:imageUrl")
        fun loadImage(view: ImageView, imageUrl: String) {
            val mPicasso = Picasso.get()
            // if (BuildConfig.DEBUG) mPicasso.setIndicatorsEnabled(true)
            mPicasso.load(imageUrl).into(view)
        }
    }

}
