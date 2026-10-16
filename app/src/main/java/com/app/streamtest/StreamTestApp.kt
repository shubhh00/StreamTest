package com.app.streamtest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp starts Hilt: it generates the app-wide dependency container
// that lives as long as the app process.
@HiltAndroidApp
class StreamTestApp : Application()
