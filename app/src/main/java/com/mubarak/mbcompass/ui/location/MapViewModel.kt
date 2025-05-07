package com.mubarak.mbcompass.ui.location

import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    var latitude: Double? = null
    var longitude: Double? = null
    var zoom: Int = 13
}