package com.inspiredmedia.storywriter

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import java.net.MalformedURLException
import java.net.URL

class ConsentInfo {
    companion object {
        fun showBox(context: Context) {

            var form: ConsentForm? = null

            val consentInformation = ConsentInformation.getInstance(context.applicationContext)
            val publisherIds = arrayOf("pub-8569563470793396")
            consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus?) {

                }

                override fun onFailedToUpdateConsentInfo(reason: String?) {
                    Toast.makeText(context.applicationContext, "Failed to load consent", Toast.LENGTH_SHORT).show()
                }
            })

            var privacyUrl: URL? = null
            try {
                privacyUrl =
                    URL("https://raw.githubusercontent.com/RobertJaskowski/RobertJaskowski.github.io/master/privacypolicy")
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                // Handle error.
            }

            form = ConsentForm.Builder(context, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        form!!.show()
                    }

                    override fun onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    override fun onConsentFormClosed(
                        consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?
                    ) {
                        // Consent form was closed.
                        val extras = Bundle()
                        extras.putString("npa", "1")

                        val request = AdRequest.Builder()
                            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                            .build()


                    }

                    override fun onConsentFormError(errorDescription: String?) {
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build()

            form!!.load()


        }
    }
}