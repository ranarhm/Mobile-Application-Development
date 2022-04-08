package edu.vt.cs.cs5254.dreamcatcher

import android.util.Log
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.room.Index
import java.io.File
import java.lang.IllegalArgumentException
import java.util.Date
import java.util.UUID

class DreamDetailViewModel : ViewModel() {
    private val dreamRepository = DreamRepository.get()
    private val dreamIdLiveData = MutableLiveData<UUID>()
    var dreamLiveData: LiveData<DreamWithEntries> =
        Transformations.switchMap(dreamIdLiveData) { dreamId ->
            dreamRepository.getDreamWithEntries(dreamId)
        }

    fun loadDream(dreamId: UUID) {
        dreamIdLiveData.value = dreamId
    }

    fun saveDream() {
        dreamLiveData.value?.let {
            dreamRepository.updateDreamWithEntries(it)
        }
    }

    fun refreshDreamEntries(dreamEntries: List<DreamEntry>) {
        Log.d("test", "${dreamEntries.size}")
        dreamLiveData.value?.let {
            it.dreamEntries = dreamEntries
            dreamRepository.updateDreamWithEntries(it)
        }
    }

    fun getPhotoFile(dreamWithEntries: DreamWithEntries): File {
        return dreamRepository.getPhotoFile(dreamWithEntries.dream)
    }
}