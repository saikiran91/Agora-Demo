package io.agora.agorademo.features.base;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.agora.agorabase.openlive.AGApplication;
import io.agora.agorabase.openlive.model.EngineConfig;
import io.agora.agorabase.openlive.model.MyEngineEventHandler;
import io.agora.agorabase.openlive.model.WorkerThread;
import io.agora.rtc.RtcEngine;

public abstract class AgoraBaseActivity extends MvpBaseActivity {
    private final static Logger log = LoggerFactory.getLogger(AgoraBaseActivity.class);

    protected abstract void initUIandEvent();

    protected abstract void deInitUIandEvent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AGApplication) getApplication()).initWorkerThread();
        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initUIandEvent();
            }
        });
    }

    @Override
    protected void onStop() {
        deInitUIandEvent();
        finish();
        super.onStop();
    }

    protected RtcEngine rtcEngine() {
        return ((AGApplication) getApplication()).getWorkerThread().getRtcEngine();
    }

    protected final WorkerThread worker() {
        return ((AGApplication) getApplication()).getWorkerThread();
    }

    protected final EngineConfig config() {
        return ((AGApplication) getApplication()).getWorkerThread().getEngineConfig();
    }

    protected final MyEngineEventHandler event() {
        return ((AGApplication) getApplication()).getWorkerThread().eventHandler();
    }
}