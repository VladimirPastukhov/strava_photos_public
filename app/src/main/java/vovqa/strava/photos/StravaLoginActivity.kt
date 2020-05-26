package vovqa.strava.photos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import vovqa.strava.photos.Api.clientId

class StravaLoginActivity : Activity(){

    override fun onResume() {
        super.onResume()
        startAuth()
    }

    private fun startAuth() {
        val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", "localhost://vovqa.strava.photos")
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        startActivity(intent)
        finish()
    }
}