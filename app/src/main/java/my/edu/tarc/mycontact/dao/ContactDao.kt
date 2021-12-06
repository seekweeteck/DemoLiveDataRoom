package my.edu.tarc.mycontact.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import my.edu.tarc.mycontact.model.Contact

@Dao
interface ContactDao {
    @Query("SELECT * FROM contact ORDER BY name ASC")
    fun getAllContact(): LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: Contact) //async task

    @Delete
    suspend fun delete(contact: Contact) //async task
}