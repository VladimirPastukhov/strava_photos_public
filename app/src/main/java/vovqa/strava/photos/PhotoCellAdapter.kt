package vovqa.strava.photos

import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.centerCropTransform


class PhotoCellAdapter(
    val context: Context,
    val photoUrls: Array<String>,
    val anySelectedListener: (Boolean) -> Unit = {}
)
    : RecyclerView.Adapter<PhotoCellAdapter.ViewHolder>()
{
    private val flags = photoUrls.map { it to Flag() }.toMap()

    override fun getItemCount() = photoUrls.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PhotoCellAdapter.ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.image_cell, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        val url = photoUrls[i]

        viewHolder.itemView.setOnClickListener{ openInBrowser(url) }

        flags[url]!!.checkBox = viewHolder.checkBox

        Glide.with(context)
            .load(url)
            .apply(centerCropTransform())
            .fitCenter()
            .placeholder(R.drawable.ic_img_loading)
            .error(R.drawable.ic_img_error)
            .into(viewHolder.img)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.image_view)
        val checkBox: CheckBox = view.findViewById(R.id.check_box)
    }

    fun openInBrowser(url: String) = context.startActivity(
        Intent(ACTION_VIEW).apply {
            flags += FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(url)
        }
    )

    fun isPhotoSelected(photoUrl: String) = flags[photoUrl]?.value ?: false
    fun selectAll() {
        flags.values.forEach{it.value = true}
        anySelectedListener(true)
    }

    fun deSelectAll() {
        flags.values.forEach{it.value = false}
        anySelectedListener(false)
    }

    fun isAnySelected() = flags.any {  it.value.value }

    private inner class Flag(){
        var value: Boolean = true
            set(v) {
                field = v;
                checkBox?.setOnCheckedChangeListener(null)
                checkBox?.isChecked = value
                checkBox?.setOnCheckedChangeListener{ _, isChecked -> value = isChecked }
                anySelectedListener(isAnySelected())
            }
        var checkBox: CheckBox? = null
            set(v) {
                field = v
                checkBox?.setOnCheckedChangeListener(null)
                checkBox?.isChecked = value
                checkBox?.setOnCheckedChangeListener{ _, isChecked -> value = isChecked  }
            }
    }
}