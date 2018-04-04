package ru.spbau.mit.structurednotes.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

abstract class CardAttribute

@Parcelize
class Photo : CardAttribute(), Parcelable

@Parcelize
class Audio: CardAttribute(), Parcelable

@Parcelize
class GPS(val auto: Boolean): CardAttribute(), Parcelable

@Parcelize
class ShortText(val label: String): CardAttribute(), Parcelable

@Parcelize
class LongText(val label: String): CardAttribute(), Parcelable

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

    fun shortText(label: String) {
        layout.add(ShortText(label))
    }

    fun longText(label: String) {
        layout.add(ShortText(label))
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