package my.edu.tarc.mycontact.ui.profile

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import my.edu.tarc.mycontact.MainActivity
import my.edu.tarc.mycontact.R
import my.edu.tarc.mycontact.databinding.FragmentProfileBinding
import my.edu.tarc.mycontact.model.Contact


class ProfileFragment : Fragment() {
    //View Binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var filePath : Uri //to hold file path of profile picture

    //@RequiresApi(Build.VERSION_CODES.P)
    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if(result.resultCode == RESULT_OK){

            /*val source: ImageDecoder.Source = ImageDecoder.createSource(context?.applicationContext!!, filePath)
            var bitmap: Bitmap = ImageDecoder.decodeBitmap(source)*/

            filePath = result.data?.data!!
            binding.imageViewPic.setImageURI(filePath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Enable menu in this fragment
        setHasOptionsMenu(true)
        Log.d("Profile Fragment", "OnCreate")
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val sharedPreferences = activity?.getPreferences(MODE_PRIVATE)
        val name = sharedPreferences?.getString(MainActivity.NAME, "")
        val phone = sharedPreferences?.getString(MainActivity.PHONE, "")

        binding.editTextTextPersonName.setText(name)
        binding.editTextPhone.setText(phone)
        /* try{
                val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val file = File(dir, phone + ".jpg")
            }*/
        binding.imageViewPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            //register a getContent
            getContent.launch(intent)
        }

        Log.d("Profile Fragment", "OnCreateView")
        return view
    }

    //TODO: Insert code to display and handle Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_profile_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_save -> {
                //TODO: Save profile data
                //Log.d("Profile Fragment", "Profile saved")
                if(binding.editTextPhone.text.isBlank()){
                    binding.editTextPhone.error = getString(R.string.error_value_required)
                    return false
                }
                if(binding.editTextTextPersonName.text.isBlank()){
                    binding.editTextTextPersonName.error = getString(R.string.error_value_required)
                    return false
                }

                val contact = Contact(binding.editTextTextPersonName.text.toString(),
                                        binding.editTextPhone.text.toString())
                //Snackbar.make(binding.frameLayout, R.string.record_saved, Snackbar.LENGTH_SHORT).show()
                Toast.makeText(context, R.string.record_saved, Toast.LENGTH_SHORT).show()

                val builder = AlertDialog.Builder(context)

                builder.setMessage("Save profile?")
                    .setPositiveButton("Save") { dialog, id ->
                        //TODO Save profile record
                        saveProfilePreference(contact)
                    }
                    .setNegativeButton("Cancel", {dialog, id ->
                        //TODO Cancel the save event
                    })
                builder.create().show()

                true //return
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveProfilePreference(contact :Contact){
        val sharedPreferences = activity?.getPreferences(MODE_PRIVATE)
        sharedPreferences?.edit()?.apply {
            putString(MainActivity.NAME, contact.name)
            putString(MainActivity.PHONE, contact.phone)
            apply()
        }

        //Write profile record to the Firebase
        val database: DatabaseReference
        database = Firebase.database.reference

        database.child("profile").child(contact.phone).child("name").setValue(contact.name)
        database.child("profile").child(contact.phone).child("phone").setValue(contact.phone)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Profile Fragment", "OnDestroy")
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        Log.d("Profile Fragment", "onPause")
    }

}