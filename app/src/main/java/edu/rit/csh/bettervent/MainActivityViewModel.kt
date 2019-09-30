package edu.rit.csh.bettervent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.collections.ArrayList

class MainActivityViewModel: ViewModel(){
    private val events: MutableLiveData<ArrayList<Event>> by lazy {
        MutableLiveData<ArrayList<Event>>().also{
            loadEvents()
        }
    }

    fun getEvents(): LiveData<ArrayList<Event>> {
        return events
    }

    private fun loadEvents(): ArrayList<Event>{
        //TODO: get the Events
        return ArrayList<Event>()
    }

    fun addEvent(e: Event){
        events.value?.add(e)
    }
}
