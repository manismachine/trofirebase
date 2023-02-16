package com.torvalds.rtdbdatasaervice

import android.app.Application
import com.torvalds.rtdbdatasaervice.gameover.Marker

class app  : Application() {
    companion object {
        var marker: Marker? = null
    }
}