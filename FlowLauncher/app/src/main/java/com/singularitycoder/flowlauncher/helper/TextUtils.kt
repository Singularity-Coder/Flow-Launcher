package com.singularitycoder.flowlauncher.helper

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Context?.clipboard(): ClipboardManager? =
    this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

fun EditText.disableCopyPaste() {
    CoroutineScope(Dispatchers.IO).launch {
        context.clipboard()?.addPrimaryClipChangedListener {
            context.clipboard()?.text = ""
        }
        isLongClickable = false
        setTextIsSelectable(false)
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean = false
            override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean = false
            override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean = false
            override fun onDestroyActionMode(p0: ActionMode?) = Unit
        }
    }
}

fun EditText.setDigits(allowedChars: String) {
    keyListener = DigitsKeyListener.getInstance(allowedChars)
    setRawInputType(InputType.TYPE_CLASS_TEXT)
}

/** Request focus before showing keyboard - editText.requestFocus() */
fun EditText?.showKeyboard() {
    if (this?.hasFocus() == true) {
        val imm =
            this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

/** Request focus before hiding keyboard - editText.requestFocus() */
fun EditText?.hideKeyboard() {
    if (this?.hasFocus() == true) {
        val imm = this.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    // Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    // If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// https://stackoverflow.com/questions/4745988/how-do-i-detect-if-software-keyboard-is-visible-on-android-device-or-not
val View.isKeyboardVisible: Boolean
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        WindowInsetsCompat
            .toWindowInsetsCompat(rootWindowInsets)
            .isVisible(WindowInsetsCompat.Type.ime())
    } else {
        false
    }
