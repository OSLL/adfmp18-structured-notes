package ru.spbau.mit.structurednotes.data

import kotlinx.serialization.Serializable

@Serializable
data class NoteData(val data: List<List<String>>)

@Serializable
data class CardData(val data: MutableList<NoteData>)

@Serializable
data class DB(val data: MutableMap<Int, CardData>)

@Serializable
data class Cards(val data: MutableList<CardType>)
