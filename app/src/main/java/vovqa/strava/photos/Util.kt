package vovqa.strava.photos


import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import vovqa.strava.photos.Api.StravaApi
import vovqa.strava.photos.Api.Activity
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

const val ACTIVITIES_PER_PAGE = 40

suspend fun List<File>.zip(
    context: Context,
    filename: String,
    onProgress: suspend () -> Unit = {}
): Uri {
    val zipFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
    val zipStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

    forEach{ file ->
        val photoStream = FileInputStream(file)
        zipStream.putNextEntry(ZipEntry(file.name))

        photoStream.transferTo(zipStream)

        zipStream.closeEntry()
        photoStream.close()
        onProgress()
    }

    zipStream.close()
    return zipFile.toShareableUri(context)
}

fun InputStream.transferTo(out: OutputStream){
    val buffer = ByteArray(1024)
    var length = this.read(buffer)
    while (length > 0) {
        out.write(buffer, 0, length)
        length = this.read(buffer)
    }
}

fun File.toShareableUri(context: Context): Uri
        = FileProvider.getUriForFile(context, context.applicationContext.packageName+".provider", this)

suspend fun loadAllActivities(api: StravaApi, onProgress: suspend () -> Unit = {}): List<Activity> = coroutineScope {
    val pages = mutableListOf<List<Activity>>()
    val pageRanges = (1 .. Int.MAX_VALUE).asSequence().chunked(4).iterator()

    do {
        pages += pageRanges.next().map{ pageNum ->
            async {
                api.getActivities(ACTIVITIES_PER_PAGE, pageNum).apply { onProgress()  }
            }
        }.awaitAll()
    }while( pages.all{ it.isNotEmpty() } )

    pages.flatten()
}


suspend fun loadPhotoLinks(
    api: StravaApi, activities: List<Activity>, onProgress: suspend () -> Unit = {}
): List<String> = coroutineScope {
    activities.map { activity ->
            async {
                api.getActivityPhotosBig(activity.id).apply { onProgress() }
            }
        }.awaitAll()
        .flatten()
        .map { photo -> photo.urls.url }
}


