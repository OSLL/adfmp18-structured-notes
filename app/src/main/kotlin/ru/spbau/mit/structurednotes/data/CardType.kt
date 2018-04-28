package ru.spbau.mit.structurednotes.data

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.SeekBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.constructor_text_conf.view.*
import kotlinx.android.synthetic.main.list_audio.view.*
import kotlinx.android.synthetic.main.list_long.view.*
import kotlinx.android.synthetic.main.list_photo.view.*
import kotlinx.android.synthetic.main.list_short.view.*
import kotlinx.android.synthetic.main.map_button.view.*
import kotlinx.android.synthetic.main.note_audio.view.*
import kotlinx.android.synthetic.main.note_audio_record.view.*
import kotlinx.android.synthetic.main.short_note.view.*
import kotlinx.serialization.*
import org.jetbrains.anko.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.ui.note.createImageFile
import ru.spbau.mit.structurednotes.utils.inflate

@Serializable
abstract class CardAttribute {
    abstract fun injectToConstructor(ctx: Context, itemView: ViewGroup)
    abstract fun injectToNote(noteActivity: NoteActivity, itemView: ViewGroup): View
    abstract fun injectToList(listActivity: ListActivity, noteView: ViewGroup, data: List<String>)

    @Serializer(forClass = CardAttribute::class)
    companion object : KSerializer<CardAttribute> {
        override val serialClassDesc: KSerialClassDesc
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override fun load(input: KInput): CardAttribute {
            val canonicalName = input.readStringValue()
            return input.readSerializableValue(serializerByClass(Class.forName(canonicalName).kotlin))
        }

        override fun save(output: KOutput, obj: CardAttribute) {
            val canonicalName = obj.javaClass.canonicalName
            output.writeStringValue(canonicalName)
            output.writeSerializableValue(serializerByClass(Class.forName(canonicalName).kotlin), obj)
        }

    }
}

@Serializable
class Photo : CardAttribute() {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_photo)
        itemView.addView(imageView)
    }

    override fun injectToList(listActivity: ListActivity, noteView: ViewGroup, data: List<String>) {
        val photos = noteView.inflate(R.layout.list_photo) as ViewGroup

        for (photoUri in data) {
            val uri = Uri.parse(photoUri)
            val bitmap = MediaStore.Images.Media.getBitmap(listActivity.contentResolver, uri)
            val bitmapScaled =  Bitmap.createScaledBitmap(bitmap, bitmap.getScaledWidth(80), bitmap.getScaledHeight(80), true)

            val imageView = ImageView(listActivity)
            imageView.setImageBitmap(bitmapScaled)
            photos.list_photo_photos.addView(imageView)
        }

        noteView.addView(photos)
    }


    override fun injectToNote(noteActivity: NoteActivity, itemView: ViewGroup) = itemView.inflate(R.layout.note_photo)
}

@Serializable
class Audio: CardAttribute() {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_audio)
        itemView.addView(imageView)
    }

    override fun injectToList(listActivity: ListActivity, noteView: ViewGroup, data: List<String>) {
        val view = noteView.inflate(R.layout.list_audio).also {
            it.list_audio_records.adapter = RecyclerAdapter(listActivity, data)
            it.list_audio_records.layoutManager = LinearLayoutManager(listActivity)
        }

        noteView.addView(view)
    }

    override fun injectToNote(noteActivity: NoteActivity, itemView: ViewGroup) = itemView.inflate(R.layout.note_audio).also {
        it.note_audio_records.adapter = RecyclerAdapter(noteActivity, noteActivity.audioData())
        it.note_audio_records.layoutManager = LinearLayoutManager(noteActivity)
    }

    class RecyclerAdapter(val ctx: Context, val data: List<String>) : RecyclerView.Adapter<RecyclerHolder>() {
        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
            holder.bindTo(Uri.parse(data[position]))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                RecyclerHolder(ctx, parent.inflate(R.layout.note_audio_record))
    }

    class RecyclerHolder(val ctx: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progress = 0
        lateinit var mediaPlayer: MediaPlayer
        val handler = Handler()
        val period = 300


        fun bindTo(uri: Uri) {
            mediaPlayer = MediaPlayer.create(ctx, uri)

            itemView.progress.max = mediaPlayer.duration / period  - 1
            itemView.progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress * period)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}

            })

            itemView.play_button.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                    handler.post(object : Runnable {
                        override fun run() {
                            val self = this
                            ctx.runOnUiThread {
                                if (mediaPlayer.isPlaying) {
                                    itemView.progress.progress = mediaPlayer.currentPosition / period
                                    handler.postDelayed(self, period.toLong())
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}

@Serializable
class GPS(val auto: Boolean): CardAttribute() {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.img_map)
        itemView.addView(imageView)
    }

    override fun injectToList(listActivity: ListActivity, noteView: ViewGroup, data: List<String>) {
        noteView.inflate(R.layout.map_button, false).also {
            it.mapButton.scaleType = ImageView.ScaleType.CENTER_CROP
            it.mapButton.setImageURI(Uri.parse(data[2]))
            it.mapButton.setOnClickListener {
                AlertDialog.Builder(listActivity).also {
                    val mapView = MapView(listActivity).also {
                        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700)
                        it.onCreate(Bundle())
                        it.onStart()
                        it.onResume()
                        it.getMapAsync { map ->
                            val pos = LatLng(data[0].toDouble(), data[1].toDouble())
                            map.addMarker(MarkerOptions().position(pos))
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
                        }
                        listActivity.mapView = it
                    }

                    it.setView(mapView)
                    it.setPositiveButton("close", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {}
                    })

                    it.create()
                    it.show()
                }
            }

            noteView.addView(it)
        }
    }

    override fun injectToNote(noteActivity: NoteActivity, itemView: ViewGroup): View {
        noteActivity.gpsData().also {
            it.add("lat")
            it.add("lng")
            it.add("img")
        }

        return itemView.inflate(R.layout.map_button, false).also { imageButtonLayout ->
            imageButtonLayout.mapButton.setOnClickListener {
                AlertDialog.Builder(noteActivity).also {
                    val mapView = MapView(noteActivity).also {
                        it.getMapAsync { map ->
                            if (noteActivity.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                if (auto) {
                                    val locationManager = noteActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                                    val markerOptions = MarkerOptions().position(LatLng(.0, .0)).title("you are here")
                                    var marker: Marker? = null
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, object : LocationListener {
                                        override fun onLocationChanged(p0: Location?) {
                                            marker?.remove()
                                            val location = p0!!
                                            val latLng = LatLng(location.latitude, location.longitude)
                                            marker = map.addMarker(markerOptions.position(latLng))
                                            map.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                                            noteActivity.gpsData()[0] = latLng.latitude.toString()
                                            noteActivity.gpsData()[1] = latLng.longitude.toString()
                                        }

                                        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

                                        override fun onProviderEnabled(p0: String?) {}

                                        override fun onProviderDisabled(p0: String?) {}

                                    })
                                } else {
                                    val markerOptions = MarkerOptions().position(LatLng(.0, .0))
                                    var marker: Marker? = null
                                    map.isMyLocationEnabled = true
                                    map.setOnMapClickListener { pos ->
                                        marker?.remove()
                                        marker = map.addMarker(markerOptions.position(pos))
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, map.cameraPosition.zoom))

                                        noteActivity.gpsData()[0] = pos.latitude.toString()
                                        noteActivity.gpsData()[1] = pos.longitude.toString()
                                        map.snapshot { snapshot ->
                                            val file = noteActivity.createImageFile()

                                            snapshot.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
                                            val uri = Uri.fromFile(file)
                                            noteActivity.gpsData()[2] = uri.toString()
                                        }
                                    }
                                }
                            }
                        }

                        it.onCreate(Bundle())
                        it.onStart()
                        it.onResume()
                    }

                    it.setView(mapView)

                    it.setNegativeButton("cancel", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {}
                    })

                    it.setPositiveButton("ok", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            imageButtonLayout.mapButton.scaleType = ImageView.ScaleType.CENTER_CROP
                            imageButtonLayout.mapButton.setImageURI(Uri.parse(noteActivity.gpsData()[2]))
                        }
                    })

                    it.create()
                    it.show()
                }
            }
        }
    }

    companion object {
        fun constructorDialog(ctx: Context, onOk: (Boolean) -> Unit) {
            AlertDialog.Builder(ctx).apply {
                lateinit var auto: CheckBox

                setTitle("configure location")

                setView(with(AnkoContext.create(context, false)) {
                    linearLayout {
                        checkBox("auto").also {
                            auto = it
                        }
                    }
                })

                setPositiveButton("ok") { _, _ ->
                    onOk(auto.isChecked)
                }
            }.create().show()
        }
    }
}

@Serializable
class Text(val short: Boolean, val label: String): CardAttribute() {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        itemView.inflate(R.layout.short_note, true).also {
            it.short_note_label.text = label
            it.short_note_note.isEnabled = false
        }
    }

    override fun injectToNote(noteActivity: NoteActivity, itemView: ViewGroup) =
            itemView.inflate(R.layout.short_note).also {
                it.short_note_label.text = label
            }

    override fun injectToList(listActivity: ListActivity, noteView: ViewGroup, data: List<String>) {
        if (short) {
            noteView.inflate(R.layout.list_short, true).also {
                it.list_short_label.text = label
                it.list_short_text.text = data[0]
            }
        } else {
            noteView.inflate(R.layout.list_long, true).also {
                it.list_long_label.text = label
                it.list_long_text.text = data[0]
            }
        }
    }

    companion object {
        fun constructorDialog(ctx: Context, onOk: (Boolean, String) -> Unit) {
            AlertDialog.Builder(ctx).apply {
                setTitle("configure text")

                val view = ctx.layoutInflater.inflate(R.layout.constructor_text_conf, null, false)
                setView(view)

                setPositiveButton("ok") { _, _ ->
                    onOk(view.shortCheckBox.isChecked, view.labelEditText.text.toString())
                }
            }.create().show()
        }
    }
}


class CardTypeBuilder(val id: Int) {
    val layout = mutableListOf<CardAttribute>()

    var name: String? = null
    var logo: Int? = 0
    var color: Int? = 0xfffffff

    fun audio() {
        layout.add(Audio())
    }

    fun photo() {
        layout.add(Photo())
    }

    fun text(short: Boolean, label: String) {
        layout.add(Text(short, label))
    }

    fun gps(auto: Boolean) {
        layout.add(GPS(auto))
    }

    fun remove(seq: Int) {
        layout.removeAt(seq)
    }

    fun build(): CardType? {
        if (name == null || logo == null || color == null) {
            return null
        }

        return CardType(id, name!!, color!!, logo!!, layout)
    }
}

const val EXTRA_CARD_TYPE_ID = "ru.spbau.mit.structurednotes.data.CARD_TYPE_ID"
const val EXTRA_CARD_TYPE = "ru.spbau.mit.structurednotes.data.CardType"
const val EXTRA_CARD_DATA = "ru.spbau.mit.structurednotes.data.CardsData"
const val EXTRA_CARDS_DATA = "ru.spbau.mit.structurednotes.data.CARDS_DATA"

@Serializable
data class CardType(val id: Int, val name: String, val color: Int, val logo: Int, val layout: List<CardAttribute>) {
    override fun equals(other: Any?): Boolean = if (other is CardType) id == other.id else false

    override fun hashCode(): Int {
        return name.hashCode()
    }
}