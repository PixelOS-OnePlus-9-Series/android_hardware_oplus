/*
 * Copyright (C) 2021-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.doze

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.AmbientDisplayConfiguration
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.Secure.DOZE_ALWAYS_ON
import android.provider.Settings.Secure.DOZE_ENABLED
import android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE
import android.provider.Settings.Secure.DOZE_PICK_UP_GESTURE_AMBIENT
import android.util.Log
import androidx.preference.PreferenceManager

object Utils {
    private const val TAG = "DozeUtils"

    private const val DEBUG = true
    
    private const val DOZE_INTENT = "com.android.systemui.doze.pulse"

    const val ALWAYS_ON_DISPLAY = "always_on_display"
    const val DOZE_ENABLE = "doze_enable"

    const val CATEGORY_PICKUP_SENSOR = "pickup_sensor"
    const val CATEGORY_PROXIMITY_SENSOR = "proximity_sensor"

    const val GESTURE_PICK_UP_KEY = "gesture_pick_up_type"

    private fun startService(context: Context) {
        Log.d(TAG, "Starting service")
        context.startServiceAsUser(Intent(context, DozeService::class.java), UserHandle.CURRENT)
    }

    private fun stopService(context: Context) {
        Log.d(TAG, "Stopping service")
        context.stopServiceAsUser(Intent(context, DozeService::class.java), UserHandle.CURRENT)
    }

    fun checkDozeService(context: Context) {
        if (isDozeEnabled(context) && !isAlwaysOnEnabled(context) && areGesturesEnabled(context)) {
            startService(context)
        } else {
            stopService(context)
        }
    }

    fun isDozeEnabled(context: Context): Boolean {
        // return Settings.Secure.getInt(context.contentResolver, DOZE_ENABLED, 1) != 0
        return true
    }

    // fun enableDoze(context: Context, enable: Boolean): Boolean {
    //     return Settings.Secure.putInt(context.contentResolver, DOZE_ENABLED, if (enable) 1 else 0)
    // }

    fun launchDozePulse(context: Context) {
        if (DEBUG) Log.d(TAG, "Launch doze pulse")
        context.sendBroadcastAsUser(Intent(DOZE_INTENT), UserHandle(UserHandle.USER_CURRENT))
    }

    // fun enableAlwaysOn(context: Context, enable: Boolean): Boolean {
    //     return Settings.Secure.putIntForUser(
    //         context.contentResolver, DOZE_ALWAYS_ON, if (enable) 1 else 0, UserHandle.USER_CURRENT
    //     )
    // }

    fun isAlwaysOnEnabled(context: Context): Boolean {
        val isEnabled = Settings.Secure.getIntForUser(
            context.contentResolver, DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT
        ) != 0
        if (DEBUG) Log.d(TAG, "isAlwaysOnEnabled: $isEnabled")
        return isEnabled
    }

    fun alwaysOnDisplayAvailable(context: Context): Boolean {
        return AmbientDisplayConfiguration(context).alwaysOnAvailable()
    }

    // private fun isGestureEnabled(context: Context, gesture: String?): Boolean {
    //     return PreferenceManager.getDefaultSharedPreferences(context)
    //         .getBoolean(gesture, false)
    // }

    fun isPickUpEnabled(context: Context): Boolean {
        // return PreferenceManager.getDefaultSharedPreferences(context)
        //     .getString(GESTURE_PICK_UP_KEY, "0") != "0"
        val isEnabled = (Settings.Secure.getIntForUser(
            context.contentResolver, DOZE_PICK_UP_GESTURE, 0, UserHandle.USER_CURRENT
        ) == 1)
        if (DEBUG) Log.d(TAG, "isPickUpEnabled: $isEnabled")
        return isEnabled
   }

    fun isPickUpSetToWake(context: Context): Boolean {
        // return PreferenceManager.getDefaultSharedPreferences(context)
        //     .getString(GESTURE_PICK_UP_KEY, "0") == "2"
        val isSetToWake = (Settings.Secure.getIntForUser(
            context.contentResolver, DOZE_PICK_UP_GESTURE_AMBIENT, 0, UserHandle.USER_CURRENT
        ) == 0)
        if (DEBUG) Log.d(TAG, "isPickUpSetToWake: $isSetToWake")
        return isSetToWake
    }

    private fun areGesturesEnabled(context: Context): Boolean {
        val areEnabled = isPickUpEnabled(context)
        if (DEBUG) Log.d(TAG, "areGesturesEnabled $areEnabled")
        return areEnabled
    }

    fun getSensor(sm: SensorManager, type: String?): Sensor? {
        val sensor = sm.getSensorList(Sensor.TYPE_ALL).find { it.stringType == type }
        if (DEBUG) Log.d(TAG, "getSensor $type returns $sensor")
        return sensor
    }
}
