package io.agora.agorademo.features.branddetails

import android.databinding.ObservableArrayList
import com.github.nitrico.lastadapter.LastAdapter
import io.agora.agorademo.BR
import io.agora.agorademo.R
import io.agora.agorademo.data.DataManager
import io.agora.agorademo.data.local.UserPrefs
import io.agora.agorademo.data.model.Brand
import io.agora.agorademo.data.model.Broadcast
import io.agora.agorademo.databinding.ItemBroadcastBinding
import io.agora.agorademo.features.base.BasePresenter
import io.agora.agorademo.injection.ConfigPersistent
import io.agora.agorademo.util.clearAndAddAll
import io.agora.rtc.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.DateTime
import java.util.*
import javax.inject.Inject

/**
 * Created by saiki on 22-04-2018.
 **/
@ConfigPersistent
class BrandDetailsPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<BrandDetailsMvpView>() {
    lateinit var mBrand: Brand
    private val listOfBroadcast = ObservableArrayList<Broadcast>()
    private val lastAdapter: LastAdapter by lazy {
        LastAdapter(listOfBroadcast, BR.broadcast)
                .map<Broadcast, ItemBroadcastBinding>(R.layout.item_broadcast) {
                    onClick { joinBroadcast(it.binding.broadcast!!) }
                }
    }

    private fun joinBroadcast(broadcast: Broadcast) {
        UserPrefs.broadcastId = broadcast.id
        UserPrefs.broadcastChannel = broadcast.broadcast_channel
        mDataManager.updateBroadcastPeople(broadcast.id, 1)
        mvpView?.launchBroadcastActivity(broadcast, Constants.CLIENT_ROLE_AUDIENCE)
    }

    override fun attachView(mvpView: BrandDetailsMvpView) {
        super.attachView(mvpView)
        mvpView.setAdapter(lastAdapter)
    }

    fun loadBroadCasts() {
        mvpView?.showProgress(true)
        addDisposable(mDataManager.loadAndListenToBroadCasts(mBrand)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    listOfBroadcast.clearAndAddAll(result.sortedByDescending { it.people })
                    mvpView?.showProgress(false)
                }, { error ->
                    error.printStackTrace()
                    mvpView?.showError(error.message ?: "No message")
                    mvpView?.showProgress(false)

                }))
    }

    fun createBroadcast() {
        mvpView?.showProgress(true)
        UserPrefs.broadcastChannel = UUID.randomUUID().toString()
        createBroadcast(isBroadcasting = true)
    }

    private fun createBroadcast(isBroadcasting: Boolean) {

        val broadcast = Broadcast(
                id = "${mBrand.id}|${UserPrefs.id}",//This will make sure user start only one Broadcast for a Brand
                brand_id = mBrand.id,
                broadcast_channel = UserPrefs.broadcastChannel,
                live = isBroadcasting,
                start = if (isBroadcasting) DateTime.now().millis else null,
                end = if (!isBroadcasting) DateTime.now().millis else null,
                user_name = UserPrefs.name,
                user_id = UserPrefs.id,
                user_email = UserPrefs.email,
                user_image = UserPrefs.photoUrl
        )
        addDisposable(mDataManager.createBroadcast(broadcast)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mvpView?.showProgress(false)
                    if (isBroadcasting) mvpView?.launchBroadcastActivity(broadcast, Constants.CLIENT_ROLE_BROADCASTER)
                }, { error ->
                    mvpView?.showProgress(false)
                    error.printStackTrace()
                    mvpView?.showError(error.message ?: "createBroadcast Failed")

                }))
    }

    fun endBroadcast() {
        createBroadcast(false)
    }

    fun exitBroadcast() {
        mDataManager.updateBroadcastPeople(UserPrefs.broadcastId, -1)
    }
}