package ru.spbau.mit.structurednotes.ui.note

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_note.*
import org.jetbrains.anko.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*

class NoteActivity : AppCompatActivity() {

    lateinit var cardType: CardType

    val data: MutableList<MutableList<String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        cardType = intent.getParcelableExtra(EXTRA_CARD_TYPE)

        layout.addView(
                with(AnkoContext.create(this, false)) {
                    verticalLayout {
                        textView(cardType.name)
                        setBackgroundColor(cardType.color)

                        cardType.layout.forEachIndexed { index, attr ->
                            data.add(mutableListOf())

                            when (attr) {
                                is Text -> {
                                    if (attr.short) {
                                        textView(attr.label)
                                        editText().apply {
                                            tag = "data"
                                            inputType = InputType.TYPE_CLASS_TEXT
                                        }
                                    } else {
                                        textView(attr.label)
                                        editText().apply {
                                            tag = "data"
                                            inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                        }
                                    }
                                }
                                is Photo -> {
                                    imageButton(R.drawable.ic_add_photo).apply {
                                        tag = "data"
                                        setOnClickListener {
                                            Snackbar.make(it, "photo", Snackbar.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                is Audio -> {
                                    imageButton(R.drawable.ic_add_audio).apply {
                                        tag = "data"
                                        setOnClickListener {
                                            Snackbar.make(it, "audio", Snackbar.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                is GPS -> {
                                    if (attr.auto) {
                                        textView("your location is ...").apply {
                                            tag = "data"
                                        }
                                    } else {
                                        textView("map view").apply {
                                            tag = "data"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        )

        val intent = Intent()

        setResult(Activity.RESULT_CANCELED, intent)
    }

    fun onAddButtonClick(view: View) {
        layout.childrenRecursiveSequence()
                .filter { it.tag == "data" }
                .forEachIndexed { index, view ->
                    when (view) {
                        is TextView -> data[index] = mutableListOf(view.text.toString())
                    }
                }

        val intent = Intent().also {
            it.putExtra(EXTRA_CARD_TYPE, cardType)
            it.putExtra(EXTRA_CARD_DATA, CardData(data))
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }
}
