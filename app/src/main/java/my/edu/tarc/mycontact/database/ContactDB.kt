package my.edu.tarc.mycontact.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import my.edu.tarc.mycontact.dao.ContactDao
import my.edu.tarc.mycontact.model.Contact

@Database (entities = arrayOf(Contact::class), version =1, exportSchema = false)
abstract class ContactDB : RoomDatabase() {
    abstract fun contactDao(): ContactDao

    companion object{
        //Singleton prevents multiple instances of database opening at the same time
        @Volatile
        private var INSTANCE: ContactDB? = null

        fun getDatabase(context: Context): ContactDB{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }

            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDB::class.java,
                    "contact_db"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}