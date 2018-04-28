package ru.spbau.mit.structurednotes.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JSON
import java.io.File

@Serializable
data class NoteData(val data: List<List<String>>)

@Serializable
data class CardData(val data: MutableList<NoteData>)

@Serializable
data class CardsData(val data: MutableMap<Int, CardData>)

@Serializable
data class Cards(val data: MutableList<CardType>)

class PersistentDB(val dataFile: File, val cardsFile: File) {
    var cards: Cards = Cards(mutableListOf())
        private set
    var cardsData : CardsData = CardsData(mutableMapOf())
        private set

    fun load() {
        if (dataFile.exists()) {
            dataFile.readText().also {
                cardsData = if (it.isNotEmpty()) {
                    JSON.parse(it)
                } else {
                    CardsData(mutableMapOf())
                }
            }
        } else {
            cardsData.data.clear()
        }

        if (cardsFile.exists()) {
            cardsFile.readText().also {
                cards = if (it.isNotEmpty()) {
                    JSON.parse(it)
                } else {
                    Cards(mutableListOf())
                }
            }
        } else {
            cards.data.clear()
        }
    }

    fun store() {
        dataFile.writeText(JSON.stringify(cardsData))
        cardsFile.writeText(JSON.stringify(cards))
    }
}