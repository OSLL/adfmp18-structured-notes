package ru.spbau.mit.structurednotes.ui.note

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.note_photo.view.*
import kotlinx.android.synthetic.main.short_note.view.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class NoteActivity : AppCompatActivity() {

    lateinit var cardType: CardType

    val data: MutableList<MutableList<String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        cardType = intent.getParcelableExtra(EXTRA_CARD_TYPE)

        for (attr in cardType.layout) {
            data.add(mutableListOf())
            inputLayout.addView(attr.injectToNote(this, inputLayout).also { it.setBackgroundColor(cardType.color) } )
        }

        setResult(Activity.RESULT_CANCELED, Intent())
    }

    fun onPhotoButtonClick(view: View) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val imageFile = createImageFile()
            val imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            photoData().add(imageUri.toString())

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    fun onAudioButtonClick(view: View) {

    }


    fun onAddButtonClick(view: View) {
        cardType.layout.forEachIndexed { index, cardAttribute ->
            if (cardAttribute !is Text) {
                return@forEachIndexed
            }

            val text = inputLayout.getChildAt(index).short_note_note.text.toString()
            data[index] = mutableListOf(text)
        }

        val intent = Intent().also {
            it.putExtra(EXTRA_CARD_TYPE, cardType)
            it.putExtra(EXTRA_CARD_DATA, CardData(data))
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    fun photoData(): MutableList<String> {
        val position = cardType.layout.indexOfFirst { it is Photo }
        return data[position]
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val position = cardType.layout.indexOfFirst { it is Photo }

                    val imageView = ImageView(this)

                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(photoData().last()))
                    val bitmapScaled =  Bitmap.createScaledBitmap(bitmap, bitmap.getScaledWidth(50), bitmap.getScaledHeight(50), true)

                    imageView.setImageBitmap(bitmapScaled)

                    inputLayout.getChildAt(position).thumbnails.addView(imageView)
                }

                else -> error("impossible branch")
            }
        } else {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> photoData().removeAt(photoData().lastIndex)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    companion object {
        var REQUEST_IMAGE_CAPTURE = 1
    }
}
