package com.zfbml.aggregate

import android.app.Application

class ZfbmlApp : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
}
