package com.pinicius.android.gpslocationlib

interface ICustomLocationClient {

    fun isProviderEnabled() : Boolean

    fun startLocationUpdates()

    fun stopLocationUpdates()

    fun getLastLocation() : CustomLocation

}