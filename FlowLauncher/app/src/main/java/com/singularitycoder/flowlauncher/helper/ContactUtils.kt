package com.singularitycoder.flowlauncher.helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import com.singularitycoder.flowlauncher.home.model.Contact
import com.singularitycoder.flowlauncher.home.model.Sms
import java.io.InputStream
import java.text.DateFormat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// https://stackoverflow.com/questions/12562151/android-get-all-contacts
suspend fun Context.getContactsList(): List<Contact> = suspendCoroutine<List<Contact>> {
    if (isContactsPermissionGranted().not()) {
        applicationContext.showToast("Contacts permission not granted!")
        it.resume(emptyList<Contact>())
        return@suspendCoroutine
    }
    val list: MutableList<Contact> = ArrayList()
    try {
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if ((cursor?.count ?: 0) > 0) {
            while (cursor?.moveToNext() == true) {
                val id: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID) ?: 0)
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) ?: 0) > 0) {
                    val cursorInfo: Cursor? = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                    )
                    val inputStream: InputStream? = ContactsContract.Contacts.openContactPhotoInputStream(
                        contentResolver,
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                    )
                    val person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
                    val photoURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
                    while (cursorInfo?.moveToNext() == true) {
                        val info = Contact().apply {
                            this.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) ?: 0).trim()
                            this.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) ?: 0).trim()
                            this.imagePath = photoURI.toString().trim()
                            this.photo = if (inputStream != null) BitmapFactory.decodeStream(inputStream) else null
                            this.photoURI = photoURI
                        }
                        list.add(info)
                    }
                    cursorInfo?.close()
                }
            }
            cursor?.close()
        }
    } catch (_: Exception) {
    }
    it.resume(list)
}

// https://stackoverflow.com/questions/848728/how-can-i-read-sms-messages-from-the-device-programmatically-in-android
suspend fun Context.getSmsList(): List<Sms> = suspendCoroutine<List<Sms>> {
    if (isSmsPermissionGranted().not()) {
        applicationContext.showToast("SMS permission not granted!")
        it.resume(emptyList<Sms>())
        return@suspendCoroutine
    }
    val cursor: Cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null) ?: kotlin.run {
        showToast("No messages to show!")
        it.resume(emptyList<Sms>())
        return@suspendCoroutine
    }
    val smsList: MutableList<Sms> = mutableListOf<Sms>()
    try {
        val totalSMS = cursor.count
        if (cursor.moveToFirst()) {
            for (j in 0 until totalSMS) {
                val smsDate: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                val date = DateFormat.getDateTimeInstance().format(smsDate.toLong())
                val number: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val creator: String = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.CREATOR))
                val type: String = when (cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)).toInt()) {
                    Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                    Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                    Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
                    else -> "Unknown"
                }
                smsList.add(
                    Sms(
                        date = date.toString().trim(),
                        number = number.trim(),
                        body = body.trim(),
                        type = type.trim(),
                        creator = creator.trim()
                    )
                )
                cursor.moveToNext()
            }
        }
        cursor.close()
    } catch (_: Exception) {
    }
    it.resume(smsList)
}