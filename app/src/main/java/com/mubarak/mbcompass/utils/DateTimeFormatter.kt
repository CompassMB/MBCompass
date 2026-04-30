// SPDX-License-Identifier: GPL-3.0-or-later

/*
*
* Copyright (c) 2024 Mubarak Basha. All Rights Reserved.
This project is licensed under GPL-3.0. Any derivative work must keep the same license,
retain this copyright notice, and provide proper attribution.
*
* */

package com.mubarak.mbcompass.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateTimeFormatter {

    private const val ONE_HOUR_MS = 3_600_000L
    

    fun formatDurationTime(
        milliseconds: Long,
        compactFormat: Boolean = false
    ): String {

        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

        return if (compactFormat) {

            if (milliseconds < ONE_HOUR_MS) {
                "%d:%02d".format(minutes, seconds)
            } else {
                "%d:%02d".format(hours, minutes)
            }

        } else {

            if (milliseconds < ONE_HOUR_MS) {
                "${minutes}m ${seconds}s"
            } else {
                "${hours}h ${minutes}m"
            }

        }
    }

    fun formatDateString(timestamp: Long): String {

        val formatter = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.US
        )

        formatter.timeZone = TimeZone.getTimeZone("UTC")

        return formatter.format(Date(timestamp))
    }

    fun formatDateTimeString(timestamp: Long): String {

        return DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}

