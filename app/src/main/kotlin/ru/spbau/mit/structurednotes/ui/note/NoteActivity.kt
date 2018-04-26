package ru.spbau.mit.structurednotes.ui.note

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.note_audio.*
import kotlinx.android.synthetic.main.note_photo.view.*
import kotlinx.android.synthetic.main.short_note.view.*
import kotlinx.serialization.json.JSON
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

        cardType = JSON.parse(intent.getStringExtra(EXTRA_CARD_TYPE))

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

    private fun requestPermission(rationale: String, permission: String) {
        val requestPermission = {
            ActivityCompat.requestPermissions(this@NoteActivity, arrayOf(permission), 0)
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this@NoteActivity, permission)) {
            Snackbar.make(inputLayout, "We need permission to record audio", Snackbar.LENGTH_LONG)
                    .setAction("GRANT") { _ -> requestPermission() }
                    .show()
        } else {
            requestPermission()
        }
    }

    inner class Recorder {
        private var mediaRecorder: MediaRecorder? = null
        private var uri: String? = null

        fun start(): Boolean {
            val file = createAudioFile()
            uri = FileProvider.getUriForFile(baseContext, "com.example.android.fileprovider", file).toString()

            if (ActivityCompat.checkSelfPermission(baseContext, RECORD_AUDIO) != PERMISSION_GRANTED) {
                requestPermission("You asked to record audio", RECORD_AUDIO)
                return false
            }

            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setOutputFile(file.absolutePath)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.prepare()
            mediaRecorder?.start()

            return true
        }

        fun isRecordering(): Boolean {
            return mediaRecorder != null
        }

        fun stop(): String {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            return uri!!
        }
    }

    val recorder = Recorder()

    fun onAudioButtonClick(view: View) {
        if (recorder.isRecordering()) {
            val path = recorder.stop()
            audioData().add(path)

            note_audio_record.setImageResource(R.drawable.ic_add_audio)
            note_audio_records.adapter.notifyItemInserted(audioData().lastIndex)
        } else {
            if (recorder.start()) {
                note_audio_record.setImageResource(R.drawable.abc_ic_star_black_36dp)
            }
        }
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
            it.putExtra(EXTRA_CARD_TYPE, JSON.stringify(cardType))
            it.putExtra(EXTRA_CARD_DATA, JSON.stringify(NoteData(data)))
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    fun gpsData(): MutableList<String> {
        val position = cardType.layout.indexOfFirst { it is GPS }
        return data[position]
    }

    fun audioData(): MutableList<String> {
        val position = cardType.layout.indexOfFirst { it is Audio }
        return data[position]
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

    companion object {
        var REQUEST_IMAGE_CAPTURE = 1
    }
}

fun Context.createImageFile(): File {
    // Create an image file name
    val imageFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}

fun Context.createAudioFile(): File {
    val imageFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(imageFileName, ".mpeg4", storageDir)
}
