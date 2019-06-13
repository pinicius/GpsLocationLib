package com.pinicius.android.gpslocationlib

class CustomLocation(private val lat: Double, private val long: Double, private val time: Long, private val accuracy: Float) {

    fun getLatitude() : Double {
        return lat
    }

    fun getLongitude() : Double {
        return long
    }

    fun getTime() : Long {
        return time
    }

    fun getAccuracy() : Float {
        return accuracy
    }
}