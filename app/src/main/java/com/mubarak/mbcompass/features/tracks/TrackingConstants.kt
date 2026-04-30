// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.features.tracks

object TrackingConstants {

    const val DEFAULT_LATITUDE = 48.8584
    const val DEFAULT_LONGITUDE = 2.2945
    const val DEFAULT_ZOOM_LEVEL = 16.0

    const val DEFAULT_ACCURACY = 300f
    const val DEFAULT_ALTITUDE = 0.0
    const val DEFAULT_TIME = 0L
    const val UI_UPDATE_INTERVAL = 1000L

    const val WAYPOINT_INTERVAL = 1000L
    const val SIGNIFICANT_TIME_DIFFERENCE = 120000L
    const val DEFAULT_THRESHOLD_LOCATION_AGE = 3000000000L // 3 seconds in nanoseconds
    const val DEFAULT_THRESHOLD_DISTANCE = 15f
    const val LOCATION_ACCURACY = 30
    const val STOP_OVER_THRESHOLD = 300000L
    const val SAVE_TEMP_TRACK_INTERVAL = 9000L

    const val IMPLAUSIBLE_TRACK_START_SPEED = 250.0 // km/h

    const val STATE_TRACKING_NOT = 0
    const val STATE_TRACKING_ACTIVE = 1
    const val STATE_TRACKING_PAUSED = 2


}
