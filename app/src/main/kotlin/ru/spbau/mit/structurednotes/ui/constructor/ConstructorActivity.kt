package ru.spbau.mit.structurednotes.ui.constructor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_constructor.*
import org.jetbrains.anko.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.CardTypeBuilder
import ru.spbau.mit.structurednotes.data.EXTRA_CARD_TYPE

class ConstructorActivity : AppCompatActivity() {

    private val logos = listOf(R.drawable.ic_add_audio, R.drawable.ic_add_text, R.drawable.ic_add_photo)

    private val cardTypeBuilder = CardTypeBuilder().apply {
        color = Color.argb(100, 255, 0, 0)
        logo = R.drawable.ic_add_text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constructor)
        colorButton.setBackgroundColor(cardTypeBuilder.color!!)
    }

    fun onAddPhotoButtonClick(view: View) {
        cardTypeBuilder.photo()

        constructor.addView(with(AnkoContext.create(this, false)) {
            linearLayout {
                textView("photo")
            }
        })
    }

    fun onAddAudioButtonClick(view: View) {
        cardTypeBuilder.audio()

        constructor.addView(with(AnkoContext.create(this, false)) {
            linearLayout {
                textView("audio")
            }
        })
    }

    fun onAddLocationButtonClick(view: View) {
        AlertDialog.Builder(this).apply {
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
                cardTypeBuilder.gps(auto.isChecked)

                constructor.addView(with(AnkoContext.create(context, false)) {
                    linearLayout {
                        textView("location")
                    }
                })
            }
        }.create().show()
    }

    fun onAddNotificationButtonClick(view: View) {
        // pass
    }

    fun onAddTextButtonClick(view: View) {
        AlertDialog.Builder(this).apply {
            lateinit var short: CheckBox
            lateinit var editText: EditText

            setTitle("configure text")

            setView(with(AnkoContext.create(context, false)) {
                verticalLayout {
                    linearLayout {
                        checkBox("short").also { short = it }
                    }

                    linearLayout {
                        textView("label")
                        editText().also { editText = it }
                    }
                }
            })

            setPositiveButton("ok") { _, _ ->
                if (short.isChecked) {
                    cardTypeBuilder.shortText(editText.text.toString())
                } else {
                    cardTypeBuilder.longText(editText.text.toString())
                }


                constructor.addView(with(AnkoContext.create(context, false)) {
                    linearLayout {
                        textView("text")
                    }
                })
            }
        }.create().show()
    }

    fun onColorPickerClick(view: View) {
        ColorPickerDialogBuilder.with(this).apply {
            initialColors(intArrayOf(
                    Color.argb(100,0, 0, 255),
                    Color.argb(100,0, 255, 0),
                    Color.argb(100,255, 0, 0)))
            initialColor(cardTypeBuilder.color!!)
            noSliders()
            setPositiveButton("ok", { _, color, _ ->
                cardTypeBuilder.color = color
                colorButton.setBackgroundColor(color)
            })
        }.build().show()
    }

    fun onLogoButtonClick(view: View) {
        AlertDialog.Builder(this).apply {
            setView(with(AnkoContext.create(context, false)) {
                gridLayout {
                    for (logoId in logos) {
                        imageButton(logoId).apply {
                            setOnClickListener { _ ->
                                cardTypeBuilder.logo = logoId
                                logoButton.setImageResource(logoId)
                            }
                        }
                    }
                }
            })
        }.create().show()
    }

    fun onAddCardTypeClick(view: View) {
        val name = categoryNameEditText.text.toString()

        cardTypeBuilder.name = if (name.isEmpty()) null else  name

        val cardType = cardTypeBuilder.build()

        cardType ?: also {
            Toast.makeText(this@ConstructorActivity, "enter category name", Toast.LENGTH_LONG)
            return
        }

        val intent = Intent().also {
            it.putExtra(EXTRA_CARD_TYPE, cardType)
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }
}
