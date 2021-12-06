package my.edu.tarc.mycontact.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "contact")
data class Contact (val name: String,
                    @PrimaryKey val phone: String){

    override fun toString():String{
        return "$name : $phone"
    }
}