package ru.spbau.mit.structurednotes.ui.list

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*

class DynamicNoteComponent(private val cardType: CardType) : AnkoComponent<Context> {
    override fun createView(ui: AnkoContext<Context>): View = with(ui) {
        verticalLayout {
            for (attr in cardType.layout) {
                linearLayout {
                    when (attr) {
                        is ShortText -> {
                            textView(attr.label)
                            textView {
                                tag = "bind"
                                text = "default"
                            }
                        }
                        is LongText -> {
                            verticalLayout {
                                textView(attr.label)
                                textView {
                                    tag = "bind"
                                    text = "default"
                                }
                            }
                        }
                        is Photo -> {
                            imageView {
                                setImageResource(R.drawable.ic_add_photo)
                                tag = "bind"
                            }
                        }
                        is Audio -> {
                            textView("audio")
                        }
                        is GPS -> {
                            textView {
                                tag = "bind"
                                text = "default gps"
                            }
                        }
                        else -> error("impossible attribute")
                    }
                }
            }
        }
    }
}

fun View.bindData(cardType: CardType, data: List<List<String>>) {
    childrenSequence().forEachIndexed { index, view ->
        val attr = cardType.layout[index]
        val attrData = data[index][0]

        when (attr) {
            is ShortText -> {
                view.findViewWithTag<TextView>("bind").text = attrData
            }
            is LongText -> {
                view.findViewWithTag<TextView>("bind").text = attrData
            }
            is Photo -> {
                val uri = Uri.Builder().appendPath(attrData).build()
                view.findViewWithTag<ImageView>("bind").setImageURI(uri)
            }
        }
    }
}