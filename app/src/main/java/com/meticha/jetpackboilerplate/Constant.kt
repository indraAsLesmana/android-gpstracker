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
}
