package ru.spbau.mit.structurednotes.ui.attributes.gps

import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.map_button.view.*
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.linearLayout
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.GPS
import ru.spbau.mit.structurednotes.ui.attributes.CardAttributeAction
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.ui.note.createImageFile
import ru.spbau.mit.structurednotes.utils.inflate

class GPSAction(val attr: GPS) : CardAttributeAction {
    override fun injectToConstructor(constructorActivity: ConstructorActivity, itemViewGroup: ViewGroup) {
        val imageView = ImageView(constructorActivity)
        imageView.setImageResource(R.drawable.img_map)
        itemViewGroup.addView(imageView)
    }

    override fun injectToList(listActivity: ListActivity, itemViewGroup: ViewGroup, data: List<String>) {
        itemViewGroup.inflate(R.layout.map_button, false).also {
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

            itemViewGroup.addView(it)
        }
    }

    override fun injectToNote(noteActivity: NoteActivity, itemViewGroup: ViewGroup) {
        noteActivity.gpsData().also {
            it.add("lat")
            it.add("lng")
            it.add("img")
        }

        val view = itemViewGroup.inflate(R.layout.map_button, false)
        view.mapButton.setOnClickListener {
            AlertDialog.Builder(noteActivity).also {
                val mapView = MapView(noteActivity).also {
                    it.getMapAsync { map ->
                        if (noteActivity.checkCallingOrSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            if (attr.auto) {
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
                        view.mapButton.scaleType = ImageView.ScaleType.CENTER_CROP
                        view.mapButton.setImageURI(Uri.parse(noteActivity.gpsData()[2]))
                    }
                })

                it.create()
                it.show()
            }
        }

        itemViewGroup.addView(view)
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