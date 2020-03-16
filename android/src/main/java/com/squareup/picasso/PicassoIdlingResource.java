/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
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

