package ru.spbau.mit.structurednotes.data

import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.android.synthetic.main.constructor_block.view.*
import kotlinx.android.synthetic.main.constructor_text_conf.view.*
import org.jetbrains.anko.*
import ru.spbau.mit.structurednotes.R

abstract class CardAttribute {
    abstract fun injectTo(ctx: Context, itemView: ViewGroup)
}

@Parcelize
class Photo : CardAttribute(), Parcelable {
    override fun injectTo(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_photo)
        itemView.addView(imageView)
    }
}

@Parcelize
class Audio: CardAttribute(), Parcelable {
    override fun injectTo(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.ic_add_audio)
        itemView.addView(imageView)
    }
}

@Parcelize
class GPS(val auto: Boolean): CardAttribute(), Parcelable {
    override fun injectTo(ctx: Context, itemView: ViewGroup) {
        val imageView = ImageView(ctx)
        imageView.setImageResource(R.drawable.img_map)
        itemView.addView(imageView)
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
    override fun injectTo(ctx: Context, itemView: ViewGroup) {
        val linearLayout = LinearLayout(ctx)

        val labelTextView = TextView(ctx)
        labelTextView.text = "$label:"
        labelTextView.setPadding(4,4,4,4)
        linearLayout.addView(labelTextView)

        val yourTextView = TextView(ctx)
        yourTextView.text = "your text will be here"
        yourTextView.setBackgroundColor(Color.rgb(0xff, 0xff, 0xff))
        yourTextView.setPadding(4,4,4,4)
        linearLayout.addView(yourTextView)

        itemView.addView(linearLayout)

        linearLayout.setPadding(8, 8, 8, 8)
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