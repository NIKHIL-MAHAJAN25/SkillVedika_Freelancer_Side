package com.nikhil.sellerapp.Utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Any.loge(message: String, exception: Throwable? = null) {
    // We grab the class name automatically. .take(23) is an Android safety limit for Tags.
    val tag = this::class.java.simpleName.take(23)
    Log.e(tag, message, exception)
}

// 2. The Debug Logger (Optional, for non-error info)
// Usage: logd("User clicked button")
fun Any.logd(message: String) {
    val tag = this::class.java.simpleName.take(23)
    Log.d(tag, message)
}
fun Activity.snack(message:String)
{
    val rootView = findViewById<View>(android.R.id.content)
    Snackbar.make(rootView,message,Snackbar.LENGTH_SHORT).show()
}
fun Fragment.snack(message:String)
{
    Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
}
fun Activity.Navigateto(destination: Class<*>) {
    val intent = Intent(this, destination)
    startActivity(intent)
}

// Usage: startNewActivityClearStack(HomeActivity::class.java)
fun Activity.Navigatetoclear(destination: Class<*>) {
    val intent = Intent(this, destination)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
    finish()
}