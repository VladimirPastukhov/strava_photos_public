package vovqa.strava.photos.Api

import java.util.*
import android.app.Activity
import android.util.NoSuchPropertyException
import java.io.FileNotFoundException

val Activity.clientId get() = props.getProperty("client.id") ?: throw propertyException()
val Activity.clientSecret get() = props.getProperty("client.secret") ?: throw propertyException()

private val Activity.props get() = try {
    Properties().apply { load(baseContext.assets.open("client.properties")) }
} catch (e: FileNotFoundException) {
    throw propertyException()
}

private fun propertyException() = NoSuchPropertyException(
    "Please, add client.id and client.secret properties to /app/src/main/assets/client.properties; " +
            "You can get them by registration Strava api client app on Strava web site"
)