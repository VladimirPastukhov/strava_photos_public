package vovqa.strava.photos

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.load.engine.GlideException
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import java.net.UnknownHostException

fun ExceptionHandler(activity: AppCompatActivity): CoroutineExceptionHandler{

    val d = ErrorDialogHelper(activity)

    return CoroutineExceptionHandler { _, e: Throwable -> when (e) {
        is HttpException -> when (e.code()) {
            429 -> d.show("Http 429 error.\nStrava api request limit for this app reached.\nPlease, retry in 15 minutes")
            else -> d.show("Http ${e.code()} error")
        }
        is GlideException -> d.show("Photo download error.\nLooks like photo service does not response.\n Try later.")
        is UnknownHostException -> d.show("Looks like no Internet connection... closing.")
        else -> d.show("Whoops! Error :(\n" + e.message)
    }}
}
