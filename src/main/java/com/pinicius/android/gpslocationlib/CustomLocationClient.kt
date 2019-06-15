package com.pinicius.android.gpslocationlib

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


interface OnLocationChangeListener {
    fun onLocationChanged(location: CustomLocation)
}

private const val ONE_MINUTE: Long = 1000 * 60
private const val TWO_MINUTES: Long = 1000 * 60 * 2

class CustomLocationClient(context: AppCompatActivity, locationChangeListener: OnLocationChangeListener) :
    ICustomLocationClient {

    private var locationManager : LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var  locationListener : LocationListener

    private var currentBestLocation : CustomLocation? = null


    init {

        locationListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                val newLocation = CustomLocation(
                    location.latitude,
                    location.longitude,
                    location.time,
                    location.accuracy
                )

                if (isBetterLocation(newLocation, currentBestLocation)) {
                    locationChangeListener.onLocationChanged(newLocation)
                    currentBestLocation = newLocation

                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

    }

    override fun isProviderEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
            ONE_MINUTE, 15f, locationListener)
    }

    @SuppressLint("MissingPermission")
    override fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    @SuppressLint("MissingPermission")
    override fun getLastLocation(): CustomLocation {
        val location : Location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        return CustomLocation(
            location.latitude,
            location.longitude,
            location.time,
            location.accuracy
        )
    }

    fun isBetterLocation(location : CustomLocation, bestLocation: CustomLocation?) : Boolean {

        if (bestLocation == null) {
            currentBestLocation = location
            return true
        } else {
            // Check whether the new location fix is newer or older
            val timeDelta: Long = location.getTime() - bestLocation.getTime()
            val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
            val isSignificantlyOlder:Boolean = timeDelta < -TWO_MINUTES

            when {
                // If it's been more than two minutes since the current location, use the new location
                // because the user has likely moved
                isSignificantlyNewer -> return true
                // If the new location is more than two minutes older, it must be worse
                isSignificantlyOlder -> return false
            }

            // Check whether the new location fix is more or less accurate
            val isNewer: Boolean = timeDelta > 0L
            val accuracyDelta: Float = location.getAccuracy() - bestLocation.getAccuracy()
            val isLessAccurate: Boolean = accuracyDelta > 0f
            val isMoreAccurate: Boolean = accuracyDelta < 0f
            val isSignificantlyLessAccurate: Boolean = accuracyDelta > 200f

            // Determine location quality using a combination of timeliness and accuracy
            return when {
                isMoreAccurate -> true
                isNewer && !isLessAccurate -> true
                isNewer && !isSignificantlyLessAccurate -> true
                else -> false
            }
        }
    }
}