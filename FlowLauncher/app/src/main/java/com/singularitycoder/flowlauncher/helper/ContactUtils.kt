package com.singularitycoder.flowlauncher.helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import com.singularitycoder.flowlauncher.model.Contact
import java.io.InputStream

// https://stackoverflow.com/questions/12562151/android-get-all-contacts
fun Context.getContacts(): List<Contact> {
    val list: MutableList<Contact> = ArrayList()
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
                        this.name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) ?: 0)
                        this.mobileNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) ?: 0)
                        this.imagePath = photoURI.toString()
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
    return list
}