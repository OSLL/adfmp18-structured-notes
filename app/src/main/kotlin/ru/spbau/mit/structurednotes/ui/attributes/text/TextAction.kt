package ru.spbau.mit.structurednotes.ui.attributes.text

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import kotlinx.android.synthetic.main.constructor_text_conf.view.*
import kotlinx.android.synthetic.main.list_long.view.*
import kotlinx.android.synthetic.main.list_short.view.*
import kotlinx.android.synthetic.main.short_note.view.*
import org.jetbrains.anko.layoutInflater
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.Text
import ru.spbau.mit.structurednotes.ui.attributes.CardAttributeAction
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.utils.inflate

class TextAction(val attr: Text): CardAttributeAction {
    override fun injectToConstructor(constructorActivity: ConstructorActivity, itemViewGroup: ViewGroup) {
        itemViewGroup.inflate(R.layout.short_note, true).also {
            it.short_note_label.text = attr.label
            it.short_note_note.isEnabled = false
        }
    }

    override fun injectToNote(noteActivity: NoteActivity, itemViewGroup: ViewGroup) {
        val view = itemViewGroup.inflate(R.layout.short_note)
        view.short_note_label.text = attr.label

        itemViewGroup.addView(view)
    }

    override fun injectToList(listActivity: ListActivity, itemViewGroup: ViewGroup, data: List<String>) {
        if (attr.short) {
            itemViewGroup.inflate(R.layout.list_short, true).also {
                it.list_short_label.text = attr.label
                it.list_short_text.text = data[0]
            }
        } else {
            itemViewGroup.inflate(R.layout.list_long, true).also {
                it.list_long_label.text = attr.label
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