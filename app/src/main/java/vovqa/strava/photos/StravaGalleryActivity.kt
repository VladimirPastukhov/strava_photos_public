package vovqa.strava.photos

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import vovqa.strava.photos.Api.Activity
import vovqa.strava.photos.Api.clientId
import vovqa.strava.photos.Api.clientSecret
import vovqa.strava.photos.Api.createApi
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

class StravaGalleryActivity : AppCompatActivity(){

    private val PHOTOS_PER_LINE = 4
    private val recyclerView: RecyclerView by lazy{ findViewById<RecyclerView>(R.id.recyclerView) }

    private val photoAdapter: PhotoCellAdapter get() = recyclerView.adapter as PhotoCellAdapter
    private val shareButton: Button by lazy{ findViewById<Button>(R.id.share_button) }
    private val shareZipButton: Button by lazy{ findViewById<Button>(R.id.share_zip_button) }

    private val shareLinksButton: Button by lazy { findViewById<Button>(R.id.share_links_button) }
    private val progressBar = ProgressBarHelper(this)

    private var networkJob: Job? = null

    override fun onCreateOptionsMenu(menu: Menu?):Boolean {
        menuInflater.inflate(R.menu.gallery_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.select_all -> { photoAdapter.selectAll(); true }
        R.id.deselect_all -> { photoAdapter.deSelectAll(); true }
        else -> { super.onOptionsItemSelected(item) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        networkJob = loadPhotoLinks(intent?.data?.getQueryParameter("code")!!)
    }

    fun loadPhotoLinks(code: String) = progressBar.wrapIOJob{
        progressBar.forToken()
        val token = createApi().getTocken(clientId, clientSecret, code).accessToken
        val api = createApi(token)

        val ATracker = progressBar.forActivities()
        val activities: List<Activity> = loadAllActivities(api, ATracker)

        val PTracker = progressBar.forPhotoLinks(activities.size)
        val photoUrls = loadPhotoLinks(api, activities, PTracker).toTypedArray()

        withContext(Main) { setupLayout(photoUrls) }
    }

    fun setupLayout(photoUrls: Array<String>){
        recyclerView.layoutManager = GridLayoutManager(applicationContext, PHOTOS_PER_LINE)
        val anyPhotoSelectedListener: (Boolean) -> Unit = {anySelected ->
            shareButton.isVisible = anySelected
            shareZipButton.isVisible = anySelected
            shareLinksButton.isVisible = anySelected
        }
        recyclerView.adapter = PhotoCellAdapter(applicationContext, photoUrls, anyPhotoSelectedListener)
        shareButton.setOnClickListener { networkJob = sharePhotos(photoUrls) }
        shareZipButton.setOnClickListener { networkJob = sharePhotosAsZip(photoUrls) }
        shareLinksButton.setOnClickListener{ sharePhotosAsLinks(photoUrls) }
    }

    fun sharePhotosAsLinks(photoUrls: Array<String>){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, photoUrls
                .selectedOnly()
                .joinToString("\n") )
            type = "text/*"
        }
        startActivity(Intent.createChooser(intent, "Share photos to..."))
    }

    fun sharePhotos(photoUrls: Array<String>) = progressBar.wrapIOJob {
            val uriList =
                downloadAsFiles(photoUrls.selectedOnly())
                .map{ it.toShareableUri(this@StravaGalleryActivity) }
                .toCollection(ArrayList<Uri>())

            val intent = Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                putExtra(Intent.EXTRA_STREAM, uriList )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "image/jpg"
            }
            startActivity(Intent.createChooser(intent, "Share photos to..."))
        }

    fun sharePhotosAsZip(photoUrls: Array<String>) = progressBar.wrapIOJob{
        val selected = photoUrls.selectedOnly()
        val archiveUri =
            downloadAsFiles(selected)
            .zip(this@StravaGalleryActivity, "strava_photos.zip", progressBar.forPhotosZip(selected.size))

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, archiveUri )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/octet-stream"
        }
        startActivity(Intent.createChooser(intent, "Share photos to..."))
    }

    suspend fun downloadAsFiles(photoUrls: Array<String>): List<File> {
        val onProgress = progressBar.forPhotosSave(photoUrls.size)
        return photoUrls.map { photoUrl ->
            Glide.with(this).asBitmap().load(photoUrl).submit().get()
                .saveAsFile(photoUrl.takeLast(45))
                .apply { onProgress() }
        }
    }

    fun Bitmap.saveAsFile(filename: String): File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        .also { file ->
            val out = FileOutputStream(file)
            compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.close()
        }

    override fun onDestroy() {
        super.onDestroy()
        networkJob?.cancel()
    }


    fun Array<String>.selectedOnly() = filter{
            photoUrl -> photoAdapter.isPhotoSelected(photoUrl)
    }.toTypedArray()
}