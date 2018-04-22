package ru.spbau.mit.structurednotes.data

import android.arch.persistence.room.*
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database

@Entity
data class CardTypeMeta(@PrimaryKey val id: Int,
                        @ColumnInfo(name = "name") val name: String,
                        @ColumnInfo(name = "color") val color: Int,
                        @ColumnInfo(name = "logo") val logo: String,
                        @ColumnInfo(name = "attrs") val attrs: List<String>)

@Entity
data class CardTypeData(@PrimaryKey val id: Int,
                        @ColumnInfo(name = "typeId") val typeId: Int,
                        @ColumnInfo(name = "data") val data: List<List<String>>)

@Dao
interface CardTypeDataDao {
    @Query("SELECT cardTypeId, name FROM CardTypeData")
    fun allKeysWithNames(): List<CardType>

    @Query("SELECT name, attrs FROM CardTypeMeta WHERE key = id")
    fun metaById(key: Int): List<String>

    @Query("SELECT data FROM CardTypeData WHERE key = id")
    fun dataById(key: Int): List<List<String>>

    @Insert
    fun addType(id: Int, type: String)

    @Insert
    fun addData(id: Int, typeId: Int, data: List<List<String>>)

    @Query("SELECT MAX(id) + 1 FROM CardTypeMeta")
    fun nextTypeId(): Int

    @Query("SELECT MAX(id) + 1 FROM CardTypeData")
    fun nextDataId(): Int

    @Query("SELECT COUNT(*) FROM CardTypeData")
    fun cardTypeSize(): Int

    // @Delete
    // fun delete(id: Int)
}

@Database(entities = [CardTypeMeta::class, CardTypeData::class], version = 1)
abstract class CardTypeDatabase : RoomDatabase() {
    abstract fun cardTypeDataDao(): CardTypeDataDao
}
