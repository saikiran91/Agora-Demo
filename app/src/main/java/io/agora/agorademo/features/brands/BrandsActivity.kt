package io.agora.agorademo.features.brands

import android.databinding.BindingAdapter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.ImageView
import com.github.nitrico.lastadapter.LastAdapter
import com.squareup.picasso.Picasso
import io.agora.agorademo.BuildConfig
import io.agora.agorademo.R
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.util.visible
import kotlinx.android.synthetic.main.activity_brands.*
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

    companion object {
        //https://stackoverflow.com/questions/40085724/kotlin-custom-attribute-databinding
        @JvmStatic
        @BindingAdapter("bind:imageUrl")
        fun loadImage(view: ImageView, imageUrl: String) {
            val mPicasso = Picasso.get()
//            if (BuildConfig.DEBUG) mPicasso.setIndicatorsEnabled(true)
            mPicasso.load(imageUrl).into(view)
        }
    }

}
