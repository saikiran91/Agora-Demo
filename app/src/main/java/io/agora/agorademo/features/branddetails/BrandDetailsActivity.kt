package io.agora.agorademo.features.branddetails

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.github.nitrico.lastadapter.LastAdapter
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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
                    onPositiveClick = { checkForPermissionsAndStartBroadcast() }
            )
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

    }

    private fun checkForPermissionsAndStartBroadcast() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted())
                            mPresenter.createBroadcast()
                        else
                            showLongError("Required all Camera, Record Audio, Storage permission to start a broadcast.")
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    }

                }).check()
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

    fun showLongError(message: String) {
        Snackbar.make(findViewById(R.id.parent), message, Snackbar.LENGTH_LONG).show()
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

    override fun toggleEmptyView(empty: Boolean) {
        list_view.visible(!empty)
        empty_tv.visible(empty)
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


    override fun launchBroadcastActivity(broadcast: Broadcast, brand: Brand, role: Int) {
        EventBus.getDefault().postSticky(LaunchBroadCastEvent(broadcast = broadcast, brand = brand))
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
