package ru.spbau.mit.structurednotes.ui.attributes.photo

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.list_photo.view.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.ui.attributes.CardAttributeAction
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.utils.inflate

class PhotoAction : CardAttributeAction {
    override fun injectToConstructor(constructorActivity: ConstructorActivity, itemViewGroup: ViewGroup) {
        val imageView = ImageView(constructorActivity)
        imageView.setImageResource(R.drawable.ic_add_photo)
        itemViewGroup.addView(imageView)
    }

    override fun injectToNote(noteActivity: NoteActivity, itemViewGroup: ViewGroup) {
        val view = itemViewGroup.inflate(R.layout.note_photo)

        itemViewGroup.addView(view)
    }

    override fun injectToList(listActivity: ListActivity, itemViewGroup: ViewGroup, data: List<String>) {
        val photos = itemViewGroup.inflate(R.layout.list_photo) as ViewGroup

        for (photoUri in data) {
            val uri = Uri.parse(photoUri)
            val bitmap = MediaStore.Images.Media.getBitmap(listActivity.contentResolver, uri)
            val bitmapScaled = Bitmap.createScaledBitmap(bitmap, bitmap.getScaledWidth(80), bitmap.getScaledHeight(80), true)

            val imageView = ImageView(listActivity)
            imageView.setImageBitmap(bitmapScaled)
            photos.list_photo_photos.addView(imageView)
        }

        itemViewGroup.addView(photos)
    }
}