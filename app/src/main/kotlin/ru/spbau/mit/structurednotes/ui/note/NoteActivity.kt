package ru.spbau.mit.structurednotes.ui.note

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.google.android.gms.maps.MapView
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.note_audio.*
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
    var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        cardType = intent.getParcelableExtra(EXTRA_CARD_TYPE)

        for (attr in cardType.layout) {
            data.add(mutableListOf())
            inputLayout.addView(attr.injectToNote(this, inputLayout).also { it.setBackgroundColor(cardType.color) } )
        }

        mapView?.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED, Intent())
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
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

    inner class Recorder {
        private var mediaRecorder: MediaRecorder? = null
        private var uri: String? = null

        fun start() {
            val file = createAudioFile()
            uri = FileProvider.getUriForFile(baseContext, "com.example.android.fileprovider", file).toString()

            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setOutputFile(file.absolutePath)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
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
            recorder.start()

            note_audio_record.setImageResource(R.drawable.abc_ic_star_black_36dp)
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
            it.putExtra(EXTRA_CARD_TYPE, cardType)
            it.putExtra(EXTRA_CARD_DATA, CardData(data))
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

    private fun createImageFile(): File {
        // Create an image file name
        val imageFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun createAudioFile(): File {
        val imageFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".mpeg4", storageDir)
    }

    companion object {
        var REQUEST_IMAGE_CAPTURE = 1
    }
}
