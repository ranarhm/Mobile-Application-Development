package edu.vt.cs.cs5254.dreamcatcher.database

import androidx.lifecycle.LiveData
import androidx.room.*
import edu.vt.cs.cs5254.dreamcatcher.Dream
import edu.vt.cs.cs5254.dreamcatcher.DreamEntry
import edu.vt.cs.cs5254.dreamcatcher.DreamWithEntries
import java.util.UUID

@Dao
interface DreamDao {

    // ----------------------------------------------------------
    // Dream functions
    // ----------------------------------------------------------

    // TODO Implement getters / add / and update methods for Dream
    @Query("SELECT * FROM dream")
    abstract fun getDreams(): LiveData<List<Dream>>

    @Insert
    abstract fun addDream(dream: Dream)

    @Update
    abstract fun updateDream(dream: Dream)

    // ----------------------------------------------------------
    // DreamEntry functions
    // ----------------------------------------------------------

    // TODO Implement getter / delete / add / and update methods for DreamEntry
    @Query("SELECT * FROM dream WHERE id=(:dreamId)")
    abstract fun getDreamEntry(dreamId: UUID): LiveData<List<Dream>>

    @Query("DELETE FROM dream_entry WHERE dreamId=(:dreamId)")
    abstract fun deleteDreamEntries(dreamId: UUID)

    @Insert
    abstract fun addDreamEntry(dreamEntry: DreamEntry)

    @Update
    abstract fun updateDreamEntry(dreamEntry: DreamEntry)

    // ----------------------------------------------------------
    // DreamWithEntries functions
    // ----------------------------------------------------------

    @Query("SELECT * FROM dream WHERE id=(:dreamId)")
    fun getDreamWithEntries(dreamId: UUID): LiveData<DreamWithEntries>

    @Transaction
    fun updateDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        val theDream = dreamWithEntries.dream
        val theEntries = dreamWithEntries.dreamEntries
        updateDream(dreamWithEntries.dream)
        deleteDreamEntries(theDream.id)
        theEntries.forEach { e -> addDreamEntry(e) }
    }


    @Transaction
    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        addDream(dreamWithEntries.dream)
        dreamWithEntries.dreamEntries.forEach { e -> addDreamEntry(e) }
    }

    // ----------------------------------------------------------
    // Clear Dream and Entries (used for testing purposes)
    // ----------------------------------------------------------

    @Query("DELETE FROM dream")
    fun deleteAllDreamsInDatabase()

    @Query("DELETE FROM dream_entry")
    fun deleteAllDreamEntriesInDatabase()

}