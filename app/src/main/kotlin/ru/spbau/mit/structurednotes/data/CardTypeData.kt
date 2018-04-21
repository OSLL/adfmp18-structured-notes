package ru.spbau.mit.structurednotes.data

import android.arch.persistence.room.*
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database

@Entity
class CardTypeData(@PrimaryKey val cardTypeId: Int,
                   @ColumnInfo(name = "name") val name: String,
                   @ColumnInfo(name = "attrs") val attrs: List<String>,
                   @ColumnInfo(name = "attrs_data") val data: List<List<String>>)

@Dao
interface CardTypeDataDao {
    @Query("SELECT cardTypeId, name FROM CardTypeData")
    fun allKeysWithNames(): List<CardType>

    @Query("SELECT attrs FROM CardTypeData WHERE cardTypeId = id")
    fun attrsById(id: Int): List<String>

    @Query("SELECT attrs_data FROM CardTypeData WHERE cardTypeId = id")
    fun dataById(id: Int): List<List<String>>

    @Insert
    fun insert(cardTypeData: CardTypeData)

    @Delete
    fun delete(id: Int)
}

@Database(entities = [(CardTypeData::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardTypeDataDao(): CardTypeData
}
