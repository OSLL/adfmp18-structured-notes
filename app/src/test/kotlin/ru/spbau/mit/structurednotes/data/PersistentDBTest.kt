package ru.spbau.mit.structurednotes.data

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PersistentDBTest {
    val cardType1 = {
        val builder = CardTypeBuilder(10)
        builder.name = "CardType1"
        builder.text(false, "TextProp1")
        builder.gps(false)
        builder.audio()
        builder.build()!!
    }()
    val card1Data = CardData(mutableListOf(
        NoteData(listOf(listOf("some text"), listOf("0", "0"), listOf("foo.mp3", "bar.mp3")))
    ))

    val cardType2 = {
        val builder = CardTypeBuilder(20)
        builder.name = "CardType2"
        builder.text(true, "TextProp2")
        builder.photo()
        builder.gps(true)
        builder.build()!!
    }()
    val card2Data = CardData(mutableListOf(
            NoteData(listOf(listOf("some text"), listOf("some_photo.jpg"), listOf("0", "0")))
    ))

    @Rule
    @JvmField
    val folder  = TemporaryFolder()

    @Test
    fun testLoadEmpty() {
        val dataFile = folder.root.resolve("data")
        val cardsFile = folder.root.resolve("cards")
        val db = PersistentDB(dataFile, cardsFile)
        assertEquals(Cards(mutableListOf()), db.cards)
        assertEquals(CardsData(mutableMapOf()), db.cardsData)

        db.load()
        assertEquals(Cards(mutableListOf()), db.cards)
        assertEquals(CardsData(mutableMapOf()), db.cardsData)
    }

    @Test
    fun testCardTypesSaveOverride() {
        val dataFile = folder.root.resolve("data")
        val cardsFile = folder.root.resolve("cards")

        var db = PersistentDB(dataFile, cardsFile)
        db.cards.data.add(cardType1)
        db.store()

        db = PersistentDB(dataFile, cardsFile)
        assertEquals(Cards(mutableListOf()), db.cards)
        db.load()
        assertEquals(Cards(mutableListOf(cardType1)), db.cards)

        db.cards.data.add(cardType2)
        db.store()

        db = PersistentDB(dataFile, cardsFile)
        assertEquals(Cards(mutableListOf()), db.cards)
        db.load()
        assertEquals(Cards(mutableListOf(cardType1, cardType2)), db.cards)
    }

    @Test
    fun testCardsDataSaveOverride() {
        val dataFile = folder.root.resolve("data")
        val cardsFile = folder.root.resolve("cards")

        var db = PersistentDB(dataFile, cardsFile)
        db.cards.data.add(cardType1)
        db.cards.data.add(cardType2)
        db.cardsData.data[cardType1.id] = card1Data
        db.store()

        db = PersistentDB(dataFile, cardsFile)
        assertEquals(Cards(mutableListOf()), db.cards)
        assertEquals(CardsData(mutableMapOf()), db.cardsData)

        db.load()
        assertEquals(Cards(mutableListOf(cardType1, cardType2)), db.cards)
        assertEquals(CardsData(mutableMapOf(cardType1.id to card1Data)), db.cardsData)

        db.cardsData.data[cardType2.id] = card2Data
        db.store()

        db = PersistentDB(dataFile, cardsFile)
        assertEquals(Cards(mutableListOf()), db.cards)
        assertEquals(CardsData(mutableMapOf()), db.cardsData)
        db.load()

        assertEquals(Cards(mutableListOf(cardType1, cardType2)), db.cards)
        assertEquals(CardsData(mutableMapOf(cardType1.id to card1Data, cardType2.id to card2Data)), db.cardsData)
    }
}