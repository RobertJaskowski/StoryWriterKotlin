package com.inspiredmedia.storywriter.Tools

import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import android.net.ConnectivityManager
import android.content.Context
import java.util.*


class InternetManager {

    companion object {


        fun isConnectedToNetwork(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            var isConnected = false
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetworkInfo
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }

            return isConnected
        }

        fun isConnectedToNetworkWithTimer(
            context: Context,
            window: Window
        ): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            var isConnected = false
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetworkInfo
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            }
            if(isConnected)
                return true

            var snackbar = showSnackarInternetNotAvailable(window)
            startConnectevityTimer(window, context,snackbar)

            return isConnected
        }

        fun showSnackarInternetNotAvailable(window: Window): Snackbar {
            var snackbar = Snackbar.make(
                window.decorView.findViewById(com.inspiredmedia.storywriter.R.id.sendingRoot),
                "Check your internet connection",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.view.setBackgroundColor(
                ContextCompat.getColor(
                    window.context,
                    com.inspiredmedia.storywriter.R.color.colorPrimaryDark
                )
            )

            val params = snackbar.view.getLayoutParams() as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbar.view.setLayoutParams(params)
            snackbar.show()
            return snackbar
        }

        fun showSnackbarInternetIsAvailable(window: Window){
            var snackbar = Snackbar.make(
                window.decorView.findViewById(com.inspiredmedia.storywriter.R.id.sendingRoot),
                "Internet is now available. Refresh",
                Snackbar.LENGTH_SHORT
            )
            snackbar.view.setBackgroundColor(
                ContextCompat.getColor(
                    window.context,
                    com.inspiredmedia.storywriter.R.color.colorPrimaryDark
                )
            )

            val params = snackbar.view.getLayoutParams() as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbar.view.setLayoutParams(params)
            snackbar.show()
        }


        fun startConnectevityTimer(
            applicationWIndow: Window,
            context: Context,
            snackbar: Snackbar
        ) {
            val timer = Timer()
            val begin: Long = 0
            val timeInterval: Long = 5000
            timer.schedule(object : TimerTask() {
                internal var counter = 0;
                override fun run() {
                    counter++;
                    Log.e("story", "running check timer")
                    if (isConnectedToNetwork(context)) {
                        showSnackbarInternetIsAvailable(applicationWIndow)
                        timer.cancel()
                        snackbar?.dismiss()

                        Log.e("story", "cancelling timer")
                    }else if(counter > 25){
                        timer.cancel()
                        snackbar?.dismiss()
                    }
                }
            }, begin, timeInterval)
        }

    }
}