package ru.spbau.mit.structurednotes.data

import kotlinx.serialization.*

@Serializable
abstract class CardAttribute {
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
class Photo : CardAttribute()

@Serializable
class Audio: CardAttribute()

@Serializable
class GPS(val auto: Boolean): CardAttribute()

@Serializable
class Text(val short: Boolean, val label: String): CardAttribute()

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

const val EXTRA_CARD_TYPE_ID = "ru.spbau.mit.structurednotes.attr.CARD_TYPE_ID"
const val EXTRA_CARD_TYPE = "ru.spbau.mit.structurednotes.attr.CardType"
const val EXTRA_CARD_DATA = "ru.spbau.mit.structurednotes.attr.CardsData"
const val EXTRA_CARDS_DATA = "ru.spbau.mit.structurednotes.attr.CARDS_DATA"

@Serializable
data class CardType(val id: Int, val name: String, val color: Int, val logo: Int, val layout: List<CardAttribute>) {
    override fun equals(other: Any?): Boolean = if (other is CardType) id == other.id else false

    override fun hashCode(): Int {
        return name.hashCode()
    }
}