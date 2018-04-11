package ru.spbau.mit.structurednotes.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.android.synthetic.main.constructor_text_conf.view.*
import kotlinx.android.synthetic.main.list_long.view.*
import kotlinx.android.synthetic.main.list_photo.view.*
import kotlinx.android.synthetic.main.list_short.view.*
import kotlinx.android.synthetic.main.short_note.view.*
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.linearLayout
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.utils.inflate


abstract class CardAttribute {
    abstract fun injectToConstructor(ctx: Context, itemView: ViewGroup)
    abstract fun injectToNote(ctx: Context, itemView: ViewGroup): View
    abstract fun injectToList(ctx: Context, noteView: ViewGroup, data: List<String>)
}

@Parcelize
class Photo : CardAttribute(), Parcelable {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_photo)
        itemView.addView(imageView)
    }

    override fun injectToList(ctx: Context, noteView: ViewGroup, data: List<String>) {
        val photos = noteView.inflate(R.layout.list_photo) as ViewGroup

        for (photoUri in data) {
            val uri = Uri.parse(photoUri)
            val bitmap = MediaStore.Images.Media.getBitmap(ctx.contentResolver, uri)
            val bitmapScaled =  Bitmap.createScaledBitmap(bitmap, bitmap.getScaledWidth(80), bitmap.getScaledHeight(80), true)

            val imageView = ImageView(ctx)
            imageView.setImageBitmap(bitmapScaled)
            photos.list_photo_photos.addView(imageView)
        }

        noteView.addView(photos)
    }


    override fun injectToNote(ctx: Context, itemView: ViewGroup) = itemView.inflate(R.layout.note_photo)
}

@Parcelize
class Audio: CardAttribute(), Parcelable {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_audio)
        itemView.addView(imageView)
    }

    override fun injectToList(ctx: Context, noteView: ViewGroup, data: List<String>) {
        noteView.addView(TextView(ctx).also { it.text = "AUDIO" })
    }

    override fun injectToNote(ctx: Context, itemView: ViewGroup) = itemView.inflate(R.layout.note_audio)
}

@Parcelize
class GPS(val auto: Boolean): CardAttribute(), Parcelable {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.img_map)
        itemView.addView(imageView)
    }

    override fun injectToList(ctx: Context, noteView: ViewGroup, data: List<String>) {
        /*
        val textView = TextView(ctx)
        textView.text = "gps"
        noteView.addView(textView)
        */
    }

    override fun injectToNote(ctx: Context, itemView: ViewGroup): View {
        error("no")
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

@Parcelize
class Text(val short:Boolean, val label: String): CardAttribute(), Parcelable {
    override fun injectToConstructor(ctx: Context, itemView: ViewGroup) {
        itemView.inflate(R.layout.short_note, true).also {
            it.short_note_label.text = label
            it.short_note_note.isEnabled = false
        }
    }

    override fun injectToNote(ctx: Context, itemView: ViewGroup) =
            itemView.inflate(R.layout.short_note).also {
                it.short_note_label.text = label
            }

    override fun injectToList(ctx: Context, noteView: ViewGroup, data: List<String>) {
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


class CardTypeBuilder {
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

        return CardType(name!!, color!!, logo!!, layout)
    }
}

const val EXTRA_CARD_TYPE = "ru.spbau.mit.structurednotes.data.CardType"
const val EXTRA_CARD_DATA = "ru.spbau.mit.structurednotes.data.CardsData"
const val EXTRA_CARDS_DATA = "ru.spbau.mit.structurednotes.data.CARDS_DATA"

@Parcelize
data class CardType(val name: String, val color: Int, val logo: Int, val layout: List<@RawValue CardAttribute>) : Parcelable {
    override fun equals(other: Any?): Boolean = if (other is CardType) other.name == name else false

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

@Parcelize
data class CardData(val data: List<List<String>>) : Parcelable