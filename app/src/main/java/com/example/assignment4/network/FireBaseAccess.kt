package com.example.assignment4.network

import android.util.Log
import com.example.assignment4.model.PicturesSavedInformation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

interface DataCallback {
    fun onDataReceived(data: PicturesSavedInformation?)
}

class FireBaseAccess() {

    private val database = Firebase.database
    private val myRef = database.getReference("galo-e38ac")

    fun writeData(marsUrl: String, photoUrl: String, rolls: Int){
        myRef.setValue(
            PicturesSavedInformation(
                marsUrl,
                photoUrl,
                rolls
            )
        )
    }

    fun readData(callback: DataCallback) {
        myRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = snapshot.getValue(PicturesSavedInformation::class.java)
                if(value != null)
                    callback.onDataReceived(value)
                else
                    callback.onDataReceived(PicturesSavedInformation())

                Log.d("Firebase", "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Firebase", "Failed to read value.", error.toException())
            }

        })
    }

}