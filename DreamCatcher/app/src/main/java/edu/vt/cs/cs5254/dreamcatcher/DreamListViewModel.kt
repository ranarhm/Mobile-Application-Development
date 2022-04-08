package edu.vt.cs.cs5254.dreamcatcher

import androidx.lifecycle.ViewModel
import java.util.Date
import java.util.UUID

class DreamListViewModel : ViewModel() {

    private val dreamRepository = DreamRepository.get()
    val dreamListLiveData = dreamRepository.getDreams()

    fun deleteAllDreams() {
        dreamRepository.deleteAllDreamsInDatabase()
    }

    fun addDreamWithEntries(dreamWithEntries: DreamWithEntries) {
        dreamRepository.addDreamWithEntries(dreamWithEntries)
    }

}