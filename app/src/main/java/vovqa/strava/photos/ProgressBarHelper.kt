package vovqa.strava.photos

import android.app.Dialog
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.concurrent.atomic.AtomicInteger

class ProgressBarHelper(val activity: AppCompatActivity) {

    val dialog by lazy { Dialog(activity).apply {
        setContentView(R.layout.progress_bar)
        window.setLayout(MATCH_PARENT, WRAP_CONTENT)
        setCanceledOnTouchOutside(false)
    }}
    val bar by lazy { dialog.findViewById<ProgressBar>(R.id.progress_bar) }
    val text by lazy { dialog.findViewById<TextView>(R.id.progress_bar_text) }

    fun wrapIOJob(body: suspend () -> Unit) = CoroutineScope(Main + ExceptionHandler(activity)).launch{
        try {
            dialog.show()
            withContext(IO){ body() }
        } finally {
            dialog.dismiss()
        }
    }

    suspend fun forToken() = withContext(Main){
        bar.isIndeterminate = true
        text.text = "Getting token"
        withCloseAppOnCancel(true)
    }

    suspend fun forActivities(): suspend () -> Unit = withCloseAppOnCancel(true)
        .setUp(0,"Loading activities...") {"Loading activities, ${it * ACTIVITIES_PER_PAGE} loaded"}

    suspend fun forPhotoLinks(max: Int): suspend () -> Unit = withCloseAppOnCancel(true)
        .setUp(max, "Loading photo links...") {"Loading photo links, for $it activities loaded"}

    suspend fun forPhotosSave(max: Int): suspend () -> Unit = withCloseAppOnCancel(false)
        .setUp(max, "Loading photos...") {"Loading photos, $it loaded"}

    suspend fun forPhotosZip(max: Int): suspend () -> Unit = withCloseAppOnCancel(false)
        .setUp(max, "Zipping photos...") {"Zipping photos, $it zipped"}

    fun withCloseAppOnCancel(flag: Boolean) = apply { dialog.setOnCancelListener { if(flag) activity.finish() } }

    private suspend fun setUp(
        max: Int = 0,
        preProgressText: String,
        progressText: (Int) -> String
    ): suspend () -> Unit{
        withContext(Main){
            bar.max = max
            bar.isIndeterminate = max < 1
            text.text = preProgressText
        }
        val counter = AtomicInteger()
        return {
            val loadedCount = counter.incrementAndGet()
            withContext(Main) {
                bar.progress = loadedCount
                text.text = progressText(loadedCount)
            }
        }
    }
}