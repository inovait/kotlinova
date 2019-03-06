/*
 * Copyright © 2016, Connected Travel, LLC – All Rights Reserved.
 *
 * All information contained herein is property of Connected Travel, LLC including, but
 * not limited to, technical and intellectual concepts which may be embodied within.
 *
 * Dissemination or reproduction of this material is strictly forbidden unless prior written
 * permission, via license, is obtained from Connected Travel, LLC. If permission is obtained,
 * this notice, and any other such legal notices, must remain unaltered.
 */

package com.squareup.picasso;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import androidx.test.espresso.IdlingResource;
import androidx.test.runner.lifecycle.ActivityLifecycleCallback;
import androidx.test.runner.lifecycle.Stage;

import java.lang.ref.WeakReference;

/**
 * Idling resource that waits until Picasso loads all images
 * <p>
 * From https://gist.github.com/sebaslogen/0b2fdea3f322c730e04b0af7285fcd28
 */
public class PicassoIdlingResource implements IdlingResource, ActivityLifecycleCallback {

    private static final int IDLE_POLL_DELAY_MILLIS = 100;

    private IdlingResource.ResourceCallback callback;

    private WeakReference<Picasso> picassoWeakReference;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public String getName() {
        return "PicassoIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
        handler.removeCallbacksAndMessages(null);

        if (isIdle()) {
            notifyDone();
            return true;
        } else {
            /* Force a re-check of the idle state in a little while.
             * If isIdleNow() returns false,
             * Espresso only polls it every few seconds which can slow down our tests.
             */
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isIdleNow();
                }
            }, IDLE_POLL_DELAY_MILLIS);
            return false;
        }
    }

    private boolean isIdle() {
        return picassoWeakReference == null
                || picassoWeakReference.get() == null
                || picassoWeakReference.get().targetToAction.isEmpty();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }

    private void notifyDone() {
        if (callback != null) {
            callback.onTransitionToIdle();
        }
    }

    @Override
    public void onActivityLifecycleChanged(Activity activity, Stage stage) {
        switch (stage) {
            case RESUMED:
                picassoWeakReference = new WeakReference<>(Picasso.get());
                break;
            case PAUSED:
                // Clean up reference
                handler.removeCallbacksAndMessages(null);
                picassoWeakReference = null;
                callback = null;
                break;
            default: // NOP
        }
    }
}

