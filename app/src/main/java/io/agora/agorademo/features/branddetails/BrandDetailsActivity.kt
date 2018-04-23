package io.agora.agorademo.features.branddetails

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.github.nitrico.lastadapter.LastAdapter
import com.squareup.picasso.Picasso
import io.agora.agorabase.openlive.model.ConstantApp
import io.agora.agorademo.R
import io.agora.agorademo.data.local.UserPrefs
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.features.base.MvpBaseActivity
import io.agora.agorademo.features.broadcast.BroadcastActivity
import io.agora.agorademo.features.broadcast.EndLiveBroadcastEvent
import io.agora.agorademo.features.broadcast.LaunchBroadCastEvent
import io.agora.agorademo.util.regOnce
import io.agora.agorademo.util.showDialogWithAction
import io.agora.agorademo.util.unregOnce
import io.agora.agorademo.util.visible
import io.agora.rtc.Constants
import kotlinx.android.synthetic.main.activity_brand_details.*
import kotlinx.android.synthetic.main.content_brand_details.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

class BrandDetailsActivity : MvpBaseActivity(), BrandDetailsMvpView {
    @Inject
    lateinit var mPresenter: BrandDetailsPresenter
    private val eventBus: EventBus = EventBus.getDefault()
    private lateinit var brandName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        mPresenter.attachView(this)
        start_broadcast_fab.setOnClickListener {
            it.context.showDialogWithAction(
                    message = "Would you like to start broadcast for $brandName?",
                    title = "Start Broadcast",
                    onPositiveClick = { mPresenter.createBroadcast() }
            )
        }
    }

    override val layout: Int get() = R.layout.activity_brand_details


    override fun setAdapter(adapter: LastAdapter) {
        list_view.adapter = adapter
    }

    override fun showProgress(show: Boolean) {
        progressbar.visible(show)
    }

    override fun showError(message: String) {
        Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun populateUi(brand: Brand) {
        Picasso.get().load(brand.image).placeholder(R.drawable.ic_person).into(brand_image_iv)
        tag_line_tv.text = brand.description
        mPresenter.loadBroadCasts()
    }

    override fun onStart() {
        super.onStart()
        eventBus.regOnce(this)
    }

    override fun onStop() {
        eventBus.unregOnce(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun activityLaunchEvent(event: LaunchEvent) {
        brandName = event.brand.name
        mPresenter.mBrand = event.brand
        populateUi(event.brand)
        eventBus.removeStickyEvent(this)
    }


    @Subscribe(sticky = true)
    fun onEndLiveBroadcastEvent(event: EndLiveBroadcastEvent) {
        if (event.role == Constants.CLIENT_ROLE_BROADCASTER) {
            mPresenter.endBroadcast()
        } else {
            mPresenter.exitBroadcast()
        }
        eventBus.removeStickyEvent(event)
    }


    override fun launchBroadcastActivity(broadcast: Broadcast, role: Int) {
        EventBus.getDefault().postSticky(LaunchBroadCastEvent(broadcast = broadcast))
        val i = Intent(this, BroadcastActivity::class.java)
        i.putExtra(ConstantApp.ACTION_KEY_CROLE, role)
        i.putExtra(ConstantApp.ACTION_KEY_ROOM_NAME, UserPrefs.broadcastChannel)
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.detachView()
    }

    class LaunchEvent(val brand: Brand)
}
