package si.inova.kotlinova.room.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Database(
    entities = [TextEntry::class],
    version = 1
)
abstract class TestDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
}

@Dao
abstract class TestDao {
    @Query("Select * FROM entries")
    abstract fun getAllEntries(): List<TextEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertEntry(entry: TextEntry)

    @Delete
    abstract fun deleteEntry(entry: TextEntry)
}

@Entity(tableName = "entries")
data class TextEntry(@PrimaryKey val text: String)