/*
 * Copyright (c) 2015 The CyanogenMod Project
 * Copyright (c) 2017 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moto.actions;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import java.util.List;
import java.util.LinkedList;

import com.moto.actions.actions.UpdatedStateNotifier;
import com.moto.actions.actions.CameraActivationSensor;
import com.moto.actions.actions.ChopChopSensor;
import com.moto.actions.actions.FlipToMute;
import com.moto.actions.actions.LiftToSilence;
import com.moto.actions.actions.ProximitySilencer;

public class MotoActionsService extends IntentService implements UpdatedStateNotifier {
    private static final String TAG = "MotoActions";

    private final Context mContext;

    private final PowerManager mPowerManager;
    private final PowerManager.WakeLock mWakeLock;
    private final SensorHelper mSensorHelper;

    private final List<UpdatedStateNotifier> mUpdatedStateNotifiers =
                        new LinkedList<UpdatedStateNotifier>();

    public MotoActionsService(Context context) {
        super("MotoActionService");
        mContext = context;

        Log.d(TAG, "Starting");

        MotoActionsSettings motoActionsSettings = new MotoActionsSettings(context, this);
        mSensorHelper = new SensorHelper(context);

        // Other actions that are always enabled
        mUpdatedStateNotifiers.add(new CameraActivationSensor(motoActionsSettings, mSensorHelper));
        mUpdatedStateNotifiers.add(new ChopChopSensor(motoActionsSettings, mSensorHelper));
        mUpdatedStateNotifiers.add(new ProximitySilencer(motoActionsSettings, context, mSensorHelper));
        mUpdatedStateNotifiers.add(new FlipToMute(motoActionsSettings, context, mSensorHelper));
        mUpdatedStateNotifiers.add(new LiftToSilence(motoActionsSettings, context, mSensorHelper));

        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MotoActionsWakeLock");
        updateState();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    public void updateState() {
        for (UpdatedStateNotifier notifier : mUpdatedStateNotifiers) {
            notifier.updateState();
        }
    }
}
