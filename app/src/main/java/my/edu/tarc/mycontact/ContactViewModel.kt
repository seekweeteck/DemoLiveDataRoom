package my.edu.tarc.mycontact

import android.app.Application
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import my.edu.tarc.mycontact.database.ContactDB
import my.edu.tarc.mycontact.model.Contact
import my.edu.tarc.mycontact.model.Profile
import my.edu.tarc.mycontact.repository.ContactRepository

class ContactViewModel (application: Application): AndroidViewModel(application) {
    var picPath: String? = null
    var profile: Profile = Profile()

    //LiveData gives us updated contacts when they change
    var contactList: LiveData<List<Contact>>
    private val repository: ContactRepository

    init {
        val contactDao = ContactDB.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        contactList = repository.allContacts
    }

    fun insertContact(contact: Contact) = viewModelScope.launch {
        repository.insert(contact)
    }

    private fun readProfile() {
        val firebaseDatabase = Firebase.database
        val myRef = firebaseDatabase.getReference("profile").child(profile.phone.toString())

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<Profile>()
                profile = Profile(value?.name, value?.phone)
                Log.d(ContentValues.TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                val error1 = error
                val value = error1
                Log.d(ContentValues.TAG, "Value is: $value")
            }
        })
    }

    fun uploadContact() {
        if (!profile.name.isNullOrEmpty()) {
            val firebaseDatabase = Firebase.database
            val myRef = firebaseDatabase.getReference("profile")

            for (contact in contactList.value?.iterator()!!) {
                myRef.child(profile.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child(PROFILE_NAME).setValue(contact.name)
                myRef.child(profile.phone!!.toString()).child("contact_list").child(contact.phone)
                    .child(PROFILE_PHONE).setValue(contact.phone)
            }
        }else{
            Log.d(ContentValues.TAG, "Profile is null")
        }
    }

    companion object{
        const val PROFILE_NAME = "name"
        const val PROFILE_PHONE = "phone"
        const val PROFILE_PIC = "pic"
    }
}
