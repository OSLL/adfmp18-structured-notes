package ru.spbau.mit.structurednotes.ui.attributes

import android.view.ViewGroup
import ru.spbau.mit.structurednotes.data.*
import ru.spbau.mit.structurednotes.ui.attributes.audio.AudioAction
import ru.spbau.mit.structurednotes.ui.attributes.gps.GPSAction
import ru.spbau.mit.structurednotes.ui.attributes.photo.PhotoAction
import ru.spbau.mit.structurednotes.ui.attributes.text.TextAction
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity

/**
 * this package is split into many packages because ids of views in layouts have same names
 * and we want to use synthetic properties without such long names
 */

interface CardAttributeAction {
    fun injectToConstructor(constructorActivity: ConstructorActivity, itemViewGroup: ViewGroup)
    fun injectToNote(noteActivity: NoteActivity, itemViewGroup: ViewGroup)
    fun injectToList(listActivity: ListActivity, itemViewGroup: ViewGroup, data: List<String>)

    companion object {
        fun from(attr: CardAttribute) = when (attr) {
            is Photo -> PhotoAction()
            is Audio -> AudioAction()
            is GPS -> GPSAction(attr)
            is Text -> TextAction(attr)
            else -> error("impossible branch")
        }
    }
}
