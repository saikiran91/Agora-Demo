package io.agora.agorademo.features.broadcast

import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import io.agora.agorabase.openlive.model.AGEventHandler
import io.agora.agorabase.openlive.model.ConstantApp
import io.agora.agorabase.openlive.model.VideoStatusData
import io.agora.agorabase.openlive.ui.GridVideoViewContainer
import io.agora.agorabase.openlive.ui.SmallVideoViewAdapter
import io.agora.agorabase.openlive.ui.VideoViewEventListener
import io.agora.agorademo.R
import io.agora.agorademo.features.base.AgoraBaseActivity
import io.agora.agorademo.features.common.ErrorView
import io.agora.agorademo.util.*
import io.agora.rtc.Constants
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import kotlinx.android.synthetic.main.activity_live_room.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

class BroadcastActivity : AgoraBaseActivity(), AGEventHandler, BroadcastMvpView, ErrorView.ErrorListener {
    override val layout: Int get() = R.layout.activity_live_room
    @Inject
    lateinit var presenter: BroadcastPresenter
    private val hexValue = 0XFFFFFFFFL.toInt()
    private var mGridVideoViewContainer: GridVideoViewContainer? = null
    private var mSmallVideoViewDock: RelativeLayout? = null
    private val mUidsList = HashMap<Int, SurfaceView>() // uid = 0 || uid == EngineConfig.mUid
    private val isBroadcaster: Boolean get() = isBroadcaster(config().mClientRole)
    private var mSmallVideoViewAdapter: SmallVideoViewAdapter? = null
    var mViewType = VIEW_TYPE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent().inject(this)
        presenter.attachView(this)
        fab.setOnClickListener {
            question_list.visible(!question_list.isShown)
            fab.count = 0
        }
        question_list.adapter = presenter.lastAdapter
    }

    private fun askQuestion() {
        val input = EditText(this)
        input.hint = "Your question?"
        input.imeOptions = EditorInfo.IME_ACTION_DONE
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setPositiveButton("Done") { _, _ ->
            presenter.askQuestion(input.text.toString())
        }
        input.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    presenter.askQuestion(input.text.toString())
                    true
                }
                else -> false
            }
        }
        alertDialog.create()
        alertDialog.setIcon(R.drawable.ic_fab_question)
        alertDialog.setTitle("Ask Question")
        alertDialog.setView(input)
        alertDialog.show()
        input.showKeyboard()

    }


    override fun onBackPressed() {
        if (question_list.isShown) {
            question_list.hide()
        } else {
            super.onBackPressed()
        }
    }

    override fun hideQuestionList() {
        question_list.hide()
    }

    override fun updateQuestionCount() {
        fab.increase()
    }

    override fun clearQuestionCount() {
        fab.count = 0
    }

    override fun showProgress(show: Boolean) {

    }

    override fun showError(message: String) {
        message.showAsToast(applicationContext)
    }

    override fun askQuestionSuccess(message: String) {
        message.showAsToast(applicationContext)
    }

    override fun onReloadData() {

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    private fun isBroadcaster(cRole: Int): Boolean {
        return cRole == Constants.CLIENT_ROLE_BROADCASTER
    }

    private fun getRole(): Int {
        return intent.getIntExtra(ConstantApp.ACTION_KEY_CROLE, 0)
    }

    override fun initUIandEvent() {
        event().addEventHandler(this)

        val i = intent
        val cRole = i.getIntExtra(ConstantApp.ACTION_KEY_CROLE, 0)

        if (cRole == 0) {
            throw RuntimeException("Should not reach here")
        }

        val roomName = i.getStringExtra(ConstantApp.ACTION_KEY_ROOM_NAME)

        doConfigEngine(cRole)

        mGridVideoViewContainer = findViewById<View>(R.id.grid_video_view_container) as GridVideoViewContainer
        mGridVideoViewContainer!!.setItemEventHandler(VideoViewEventListener { v, item ->
            log.debug("onItemDoubleClick $v $item")

            if (mUidsList.size < 2) {
                return@VideoViewEventListener
            }

            if (mViewType == VIEW_TYPE_DEFAULT)
                switchToSmallVideoView((item as VideoStatusData).mUid)
            else
                switchToDefaultVideoView()
        })

        val button1 = findViewById<View>(R.id.btn_1) as ImageView
        val button2 = findViewById<View>(R.id.btn_2) as ImageView
        val button3 = findViewById<View>(R.id.btn_3) as ImageView

        if (isBroadcaster(cRole)) {
            buy_btn.hide()
            val surfaceV = RtcEngine.CreateRendererView(applicationContext)
            rtcEngine().setupLocalVideo(VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            surfaceV.setZOrderOnTop(true)
            surfaceV.setZOrderMediaOverlay(true)

            mUidsList.put(0, surfaceV) // get first surface view

            mGridVideoViewContainer!!.initViewContainer(applicationContext, 0, mUidsList) // first is now full view
            worker().preview(true, surfaceV, 0)
            broadcasterUI(button1, button2, button3)
        } else {
            buy_btn.show()
            audienceUI(button1, button2, button3)
            fab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fab_question));
            fab.setOnClickListener { askQuestion() }
        }

        worker().joinChannel(roomName, config().mUid)
    }

    private fun broadcasterUI(button1: ImageView, button2: ImageView, button3: ImageView) {
        button1.tag = true
        button1.setOnClickListener { v ->
            val tag = v.tag
            if (tag != null && tag as Boolean) {
                doSwitchToBroadcaster(false)
            } else {
                doSwitchToBroadcaster(true)
            }
        }
        button1.setColorFilter(resources.getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY)

        button2.setOnClickListener { worker().rtcEngine.switchCamera() }

        button3.setOnClickListener { v ->
            val tag = v.tag
            var flag = true
            if (tag != null && tag as Boolean) {
                flag = false
            }
            worker().rtcEngine.muteLocalAudioStream(flag)
            val button = v as ImageView
            button.tag = flag
            if (flag) {
                button.setColorFilter(resources.getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY)
            } else {
                button.clearColorFilter()
            }
        }
    }

    private fun audienceUI(button1: ImageView, button2: ImageView, button3: ImageView) {
        button1.tag = null
        button1.clearColorFilter()
        button1.visibility = View.GONE

        button2.visibility = View.GONE
        button3.tag = null

        button3.visibility = View.GONE
        button3.clearColorFilter()
    }

    private fun doConfigEngine(cRole: Int) {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        var prefIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX, ConstantApp.DEFAULT_PROFILE_IDX)
        if (prefIndex > ConstantApp.VIDEO_PROFILES.size - 1) {
            prefIndex = ConstantApp.DEFAULT_PROFILE_IDX
        }
        val vProfile = ConstantApp.VIDEO_PROFILES[prefIndex]

        worker().configEngine(cRole, vProfile)
    }

    override fun deInitUIandEvent() {
        doLeaveChannel()
        event().removeEventHandler(this)
        mUidsList.clear()
        EventBus.getDefault().postSticky(EndLiveBroadcastEvent(getRole()))
    }

    private fun doLeaveChannel() {
        worker().leaveChannel(config().mChannel)
        if (isBroadcaster) {
            worker().preview(false, null, 0)
        }
    }

    fun onClickClose(view: View) {
        finish()
    }

    fun onShowHideClicked(view: View) {
        var toHide = true
        if (view.tag != null && view.tag as Boolean) {
            toHide = false
        }
        view.tag = toHide

        doShowButtons(toHide)
    }

    private fun doShowButtons(hide: Boolean) {
        val topArea = findViewById<View>(R.id.top_area)
        topArea.visibility = if (hide) View.INVISIBLE else View.VISIBLE

        val button1 = findViewById<View>(R.id.btn_1)
        button1.visibility = if (hide) View.INVISIBLE else View.VISIBLE

        val button2 = findViewById<View>(R.id.btn_2)
        val button3 = findViewById<View>(R.id.btn_3)
        if (isBroadcaster) {
            button2.visibility = if (hide) View.INVISIBLE else View.VISIBLE
            button3.visibility = if (hide) View.INVISIBLE else View.VISIBLE
        } else {
            button2.visibility = View.INVISIBLE
            button3.visibility = View.INVISIBLE
        }
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {}

    private fun doSwitchToBroadcaster(broadcaster: Boolean) {
        val currentHostCount = mUidsList.size
        val uid = config().mUid
        log.debug("doSwitchToBroadcaster " + currentHostCount + " " + (uid and hexValue) + " " + broadcaster)

        if (broadcaster) {
            doConfigEngine(Constants.CLIENT_ROLE_BROADCASTER)

            Handler().postDelayed({
                doRenderRemoteUi(uid)

                val button1 = findViewById<View>(R.id.btn_1) as ImageView
                val button2 = findViewById<View>(R.id.btn_2) as ImageView
                val button3 = findViewById<View>(R.id.btn_3) as ImageView
                broadcasterUI(button1, button2, button3)

                doShowButtons(false)
            }, 1000) // wait for reconfig engine
        } else {
            stopInteraction(currentHostCount, uid)
        }
    }

    private fun stopInteraction(currentHostCount: Int, uid: Int) {
        doConfigEngine(Constants.CLIENT_ROLE_AUDIENCE)

        Handler().postDelayed({
            doRemoveRemoteUi(uid)

            val button1 = findViewById<View>(R.id.btn_1) as ImageView
            val button2 = findViewById<View>(R.id.btn_2) as ImageView
            val button3 = findViewById<View>(R.id.btn_3) as ImageView
            audienceUI(button1, button2, button3)

            doShowButtons(false)
        }, 1000) // wait for reconfig engine
    }

    private fun doRenderRemoteUi(uid: Int) {
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }

            val surfaceV = RtcEngine.CreateRendererView(applicationContext)
            surfaceV.setZOrderOnTop(true)
            surfaceV.setZOrderMediaOverlay(true)
            mUidsList.put(uid, surfaceV)
            if (config().mUid == uid) {
                rtcEngine().setupLocalVideo(VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            } else {
                rtcEngine().setupRemoteVideo(VideoCanvas(surfaceV, VideoCanvas.RENDER_MODE_HIDDEN, uid))
            }

            if (mViewType == VIEW_TYPE_DEFAULT) {
                log.debug("doRenderRemoteUi VIEW_TYPE_DEFAULT" + " " + (uid and hexValue))
                switchToDefaultVideoView()
            } else {
                val bigBgUid = mSmallVideoViewAdapter!!.exceptedUid
                log.debug("doRenderRemoteUi VIEW_TYPE_SMALL" + " " + (uid and hexValue) + " " + (bigBgUid and hexValue))
                switchToSmallVideoView(bigBgUid)
            }
        })
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }

            if (mUidsList.containsKey(uid)) {
                log.debug("already added to UI, ignore it " + (uid and hexValue) + " " + mUidsList[uid])
                return@Runnable
            }

            val isBroadcaster = isBroadcaster
            log.debug("onJoinChannelSuccess $channel $uid $elapsed $isBroadcaster")

            worker().engineConfig.mUid = uid

            val surfaceV = mUidsList.remove(0)
            if (surfaceV != null) {
                mUidsList.put(uid, surfaceV)
            }
        })
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        log.debug("onUserOffline " + (uid and hexValue) + " " + reason)
        doRemoveRemoteUi(uid)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        doRenderRemoteUi(uid)
    }

    private fun requestRemoteStreamType(currentHostCount: Int) {
        log.debug("requestRemoteStreamType $currentHostCount")
        Handler().postDelayed({
            var highest: kotlin.collections.Map.Entry<Int, SurfaceView>? = null
            for (pair in mUidsList.entries) {
                log.debug("requestRemoteStreamType " + currentHostCount + " local " + (config().mUid and hexValue) + " " + (pair.key and hexValue) + " " + pair.value.height + " " + pair.value.width)
                if (pair.key != config().mUid && (highest == null || highest.value.getHeight() < pair.value.height)) {
                    if (highest != null) {
                        rtcEngine().setRemoteVideoStreamType(highest.key, Constants.VIDEO_STREAM_LOW)
                        log.debug("setRemoteVideoStreamType switch highest VIDEO_STREAM_LOW " + currentHostCount + " " + (highest.key and hexValue) + " " + highest.value.getWidth() + " " + highest.value.getHeight())
                    }
                    highest = pair
                } else if (pair.key != config().mUid && highest != null && highest.value.getHeight() >= pair.value.height) {
                    rtcEngine().setRemoteVideoStreamType(pair.key, Constants.VIDEO_STREAM_LOW)
                    log.debug("setRemoteVideoStreamType VIDEO_STREAM_LOW " + currentHostCount + " " + (pair.key and hexValue) + " " + pair.value.width + " " + pair.value.height)
                }
            }
            if (highest != null && highest.key != 0) {
                rtcEngine().setRemoteVideoStreamType(highest.key, Constants.VIDEO_STREAM_HIGH)
                log.debug("setRemoteVideoStreamType VIDEO_STREAM_HIGH " + currentHostCount + " " + (highest.key and hexValue) + " " + highest.value.getWidth() + " " + highest.value.getHeight())
            }
        }, 500)
    }

    private fun doRemoveRemoteUi(uid: Int) {
        runOnUiThread(Runnable {
            if (isFinishing) {
                return@Runnable
            }

            mUidsList.remove(uid)

            var bigBgUid = -1
            if (mSmallVideoViewAdapter != null) {
                bigBgUid = mSmallVideoViewAdapter!!.exceptedUid
            }

            log.debug("doRemoveRemoteUi " + (uid and hexValue) + " " + (bigBgUid and hexValue))

            if (mViewType == VIEW_TYPE_DEFAULT || uid == bigBgUid) {
                switchToDefaultVideoView()
            } else {
                switchToSmallVideoView(bigBgUid)
            }
        })
    }

    private fun switchToDefaultVideoView() {
        if (mSmallVideoViewDock != null)
            mSmallVideoViewDock!!.visibility = View.GONE
        mGridVideoViewContainer!!.initViewContainer(applicationContext, config().mUid, mUidsList)

        mViewType = VIEW_TYPE_DEFAULT

        var sizeLimit = mUidsList.size
        if (sizeLimit > ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1
        }
        for (i in 0 until sizeLimit) {
            val uid = mGridVideoViewContainer!!.getItem(i).mUid
            if (config().mUid != uid) {
                rtcEngine().setRemoteVideoStreamType(uid, Constants.VIDEO_STREAM_HIGH)
                log.debug("setRemoteVideoStreamType VIDEO_STREAM_HIGH " + mUidsList.size + " " + (uid and hexValue))
            }
        }
    }

    private fun switchToSmallVideoView(uid: Int) {
        val slice = HashMap<Int, SurfaceView?>(1)
        slice.put(uid, mUidsList[uid])
        mGridVideoViewContainer!!.initViewContainer(applicationContext, uid, slice)

        bindToSmallVideoView(uid)

        mViewType = VIEW_TYPE_SMALL

        requestRemoteStreamType(mUidsList.size)
    }

    private fun bindToSmallVideoView(exceptUid: Int) {
        if (mSmallVideoViewDock == null) {
            val stub = findViewById<View>(R.id.small_video_view_dock) as ViewStub
            mSmallVideoViewDock = stub.inflate() as RelativeLayout
        }

        val recycler = findViewById<View>(R.id.small_video_view_container) as RecyclerView

        var create = false

        if (mSmallVideoViewAdapter == null) {
            create = true
            mSmallVideoViewAdapter = SmallVideoViewAdapter(this, exceptUid, mUidsList, VideoViewEventListener { v, item -> switchToDefaultVideoView() })
            mSmallVideoViewAdapter!!.setHasStableIds(true)
        }
        recycler.setHasFixedSize(true)

        recycler.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recycler.adapter = mSmallVideoViewAdapter

        recycler.isDrawingCacheEnabled = true
        recycler.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_AUTO

        if (!create) {
            mSmallVideoViewAdapter!!.notifyUiChanged(mUidsList, exceptUid, null, null)
        }
        recycler.visibility = View.VISIBLE
        mSmallVideoViewDock!!.visibility = View.VISIBLE
    }

    fun onBuyClicked(view: View) {
        "Item Added to Your Cart".showAsToast(this)
        (view as Button).isEnabled = false
    }

    companion object {

        private val log = LoggerFactory.getLogger(BroadcastActivity::class.java)

        const val VIEW_TYPE_DEFAULT = 0

        const val VIEW_TYPE_SMALL = 1
    }

    override fun onPause() {
        EventBus.getDefault().unregOnce(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().regOnce(this)
        if (isBroadcaster(getRole())) presenter.listenToNewQuestions()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLaunchBroadCastEvent(event: LaunchBroadCastEvent) {
        presenter.broadCast = event.broadcast
        room_name.text = "${event.brand.name} | ${event.broadcast.user_name}"
        EventBus.getDefault().removeStickyEvent(event)
    }
}
