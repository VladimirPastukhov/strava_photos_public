package vovqa.strava.photos

import android.app.ActionBar.LayoutParams.MATCH_PARENT
import android.app.ActionBar.LayoutParams.WRAP_CONTENT
import android.app.Dialog
import android.view.KeyEvent.KEYCODE_BACK
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ErrorDialogHelper(val activity: AppCompatActivity){

    val dialog by lazy {
        Dialog(activity).apply {
            setContentView(R.layout.error_dialog)
            window.setLayout(MATCH_PARENT, WRAP_CONTENT)
            setOnDismissListener { activity.finish() }
            setCanceledOnTouchOutside(false)
        }
    }
    val text by lazy { dialog.findViewById<TextView>(R.id.notification_text) }
    val button by lazy { dialog.findViewById<Button>(R.id.notification_button) }

    fun show(message: String){
        dialog.show()
        text.text = message
        button.setOnClickListener {
            dialog.dismiss()
        }
    }
}