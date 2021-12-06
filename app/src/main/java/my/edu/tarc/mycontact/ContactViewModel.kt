package my.edu.tarc.mycontact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import my.edu.tarc.mycontact.database.ContactDB
import my.edu.tarc.mycontact.model.Contact
import my.edu.tarc.mycontact.repository.ContactRepository

class ContactViewModel (application: Application): AndroidViewModel(application) {
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
}
