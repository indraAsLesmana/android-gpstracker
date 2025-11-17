package com.meticha.jetpackboilerplate

import java.util.concurrent.TimeUnit

object Constants {

    // retry rest api flag
    const val FEATURE_FLAG_RETRY_ON_FAIL = false
    const val MAX_RETRY_ATTEMPTS = 3
    val RETRY_INTERVAL_MILLISECONDS = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(6)
    } else {
        TimeUnit.HOURS.toMillis(2)
    }

    // Interval for sending GPS data to the server.
    val LOCATION_SEND_INTERVAL_MILLISECONDS = if (BuildConfig.DEBUG) {
        TimeUnit.MINUTES.toMillis(1)
    } else {
        TimeUnit.HOURS.toMillis(3)
    }

    // start location service on background with persistent notification enable.
    const val AUTO_START_LOCATION_SERVICE = true
}
