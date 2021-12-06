package my.edu.tarc.mycontact.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import my.edu.tarc.mycontact.dao.ContactDao
import my.edu.tarc.mycontact.model.Contact

//Serve as a middle man between application and the data source (local or remote database)
class ContactRepository (private val contactDao: ContactDao) {
    val allContacts: LiveData<List<Contact>> = contactDao.getAllContact()

    @WorkerThread
    suspend fun insert(contact: Contact){
        contactDao.insert(contact)
    }

    @WorkerThread
    suspend fun delete(contact: Contact){
        contactDao.delete(contact)
    }
}