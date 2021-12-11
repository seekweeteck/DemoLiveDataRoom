package my.edu.tarc.mycontact.ui.profile

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import my.edu.tarc.mycontact.ContactViewModel
import my.edu.tarc.mycontact.MainActivity
import my.edu.tarc.mycontact.R
import my.edu.tarc.mycontact.databinding.FragmentProfileBinding
import my.edu.tarc.mycontact.model.Contact
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


class ProfileFragment : Fragment() {
    //View Binding
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val myContactViewModel: ContactViewModel by activityViewModels()

    val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val filePath: Uri = result.data?.data!!

                val file = getUriRealPath(context?.applicationContext!!, filePath)
                binding.textViewPath.text = file

                binding.imageViewPic.setImageURI(filePath)

                //Store file path using View Model
                myContactViewModel.picPath = filePath.path.toString()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Enable menu in this fragment
        setHasOptionsMenu(true)
        Log.d("Profile Fragment", "OnCreate")
        sharedPreferences = activity?.getPreferences(MODE_PRIVATE)!!
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
        val pic = sharedPreferences?.getString(MainActivity.PICTURE, "")

        binding.editTextTextPersonName.setText(name)
        binding.editTextPhone.setText(phone)
        if (!pic.isNullOrEmpty()) {
            binding.textViewPath.text = pic.toUri().path.toString()

            val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val file = File(dir, pic)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            binding.imageViewPic.setImageBitmap(bitmap)
        }

        binding.imageViewPic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
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
        return when (item.itemId) {
            R.id.action_save -> {
                //TODO: Save profile data
                if (binding.editTextPhone.text.isBlank()) {
                    binding.editTextPhone.error = getString(R.string.error_value_required)
                    return false
                }
                if (binding.editTextTextPersonName.text.isBlank()) {
                    binding.editTextTextPersonName.error = getString(R.string.error_value_required)
                    return false
                }

                val contact = Contact(
                    binding.editTextTextPersonName.text.toString(),
                    binding.editTextPhone.text.toString()
                )
                //Snackbar.make(binding.frameLayout, R.string.record_saved, Snackbar.LENGTH_SHORT).show()
                //Toast.makeText(context, R.string.record_saved, Toast.LENGTH_SHORT).show()

                val builder = AlertDialog.Builder(context)

                builder.setMessage("Save profile?")
                    .setPositiveButton("Save") { dialog, id ->
                        //TODO Save profile record
                        saveProfilePreference(contact)
                        saveProfileDatabase(contact)
                    }
                    .setNegativeButton("Cancel", { dialog, id ->
                        //TODO Cancel the save event
                    })
                builder.create().show()

                true //return
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveProfilePreference(contact: Contact) {
        //Save a copy of profile picture to app folder
        saveImage(context?.applicationContext!!, contact.phone+".jpg")

        //Update profile picture file path to local storage
        val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        myContactViewModel.picPath = File(dir, contact.phone + ".jpg").absolutePath

        val sharedPreferences = activity?.getPreferences(MODE_PRIVATE)
        sharedPreferences?.edit()?.apply {
            putString(MainActivity.NAME, contact.name)
            putString(MainActivity.PHONE, contact.phone)
            putString(MainActivity.PICTURE, contact.phone+".jpg")
            apply()
        }
    }

    fun saveProfileDatabase(contact: Contact){
        //Write profile record to the Firebase Realtime Database
        val database: DatabaseReference
        database = Firebase.database.reference

        database.child("profile").child(contact.phone).child("name").setValue(contact.name)
        database.child("profile").child(contact.phone).child("phone").setValue(contact.phone)

        //Upload profile picture to Firebase Storage
        val myStorage = Firebase.storage("gs://my-contact2.appspot.com/")
        val myProfileImageRef = myStorage.reference.child("images").child(contact.phone)

        val dir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File(dir, contact.phone + ".jpg").absoluteFile
        val filepath = file.toUri()

        myProfileImageRef.putFile(filepath)
            .addOnSuccessListener {
                Toast.makeText(context, "File Uploaded", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener {


            }
    }

    private fun saveImage(context: Context, fileName: String) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        if (!dir.exists()) {
            dir.mkdir()
        }

        //Extract profile picture from the ImageView
        val drawable = binding.imageViewPic.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val file = File(dir, fileName)
        val outputStream: OutputStream

        try {
            outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
            Toast.makeText(context, "Picture Saved", Toast.LENGTH_SHORT).show()
            outputStream.flush()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
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

    /* This method can parse out the real local file path from a file URI.*/
    private fun getUriRealPath(ctx: Context, uri: Uri): String? {
        var ret: String? = ""
        ret = if (isAboveKitKat()) {
            // Android sdk version number bigger than 19.
            getUriRealPathAboveKitkat(ctx, uri)
        } else {
            // Android sdk version number smaller than 19.
            getImageRealPath(context?.contentResolver!!, uri, null)
        }
        return ret
    }

    /*This method will parse out the real local file path from the file content URI.
    The method is only applied to the android SDK version number that is bigger than 19.*/
    private fun getUriRealPathAboveKitkat(ctx: Context?, uri: Uri?): String? {
        var ret: String? = ""
        if (ctx != null && uri != null) {
            if (isContentUri(uri)) {
                ret = if (isGooglePhotoDoc(uri.authority)) {
                    uri.lastPathSegment
                } else {
                    getImageRealPath(context?.contentResolver!!, uri, null)
                }
            } else if (isFileUri(uri)) {
                ret = uri.path
            } else if (isDocumentUri(ctx, uri)) {

                // Get uri related document id.
                val documentId = DocumentsContract.getDocumentId(uri)

                // Get uri authority.
                val uriAuthority = uri.authority
                if (isMediaDoc(uriAuthority)) {
                    val idArr = documentId.split(":").toTypedArray()
                    if (idArr.size == 2) {
                        // First item is document type.
                        val docType = idArr[0]

                        // Second item is document real id.
                        val realDocId = idArr[1]

                        // Get content uri by document type.
                        var mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        if ("image" == docType) {
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        } else if ("video" == docType) {
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        } else if ("audio" == docType) {
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }

                        // Get where clause with real document id.
                        val whereClause = MediaStore.Images.Media._ID + " = " + realDocId
                        ret = getImageRealPath(
                            context?.contentResolver!!,
                            mediaContentUri,
                            whereClause
                        )
                    }
                } else if (isDownloadDoc(uriAuthority)) {
                    // Build download URI.
                    val downloadUri = Uri.parse("content://downloads/public_downloads")

                    // Append download document id at URI end.
                    val downloadUriAppendId =
                        ContentUris.withAppendedId(downloadUri, java.lang.Long.valueOf(documentId))
                    ret = getImageRealPath(context?.contentResolver!!, downloadUriAppendId, null)
                } else if (isExternalStoreDoc(uriAuthority)) {
                    val idArr = documentId.split(":").toTypedArray()
                    if (idArr.size == 2) {
                        val type = idArr[0]
                        val realDocId = idArr[1]
                        if ("primary".equals(type, ignoreCase = true)) {
                            ret = Environment.getExternalStorageDirectory()
                                .toString() + "/" + realDocId
                        }
                    }
                }
            }
        }
        return ret
    }

    /* Check whether the current android os version is bigger than KitKat or not. */
    private fun isAboveKitKat(): Boolean {
        var ret = false
        ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        return ret
    }

    /* Check whether this uri represent a document or not. */
    private fun isDocumentUri(ctx: Context?, uri: Uri?): Boolean {
        var ret = false
        if (ctx != null && uri != null) {
            ret = DocumentsContract.isDocumentUri(ctx, uri)
        }
        return ret
    }

    /* Check whether this URI is a content URI or not content uri like content://media/external/images/media/1302716*/
    private fun isContentUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema = uri.scheme
            if ("content".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }

    /* Check whether this URI is a file URI or not file URI like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg */
    private fun isFileUri(uri: Uri?): Boolean {
        var ret = false
        if (uri != null) {
            val uriSchema = uri.scheme
            if ("file".equals(uriSchema, ignoreCase = true)) {
                ret = true
            }
        }
        return ret
    }


    /* Check whether this document is provided by ExternalStorageProvider. Return true means the file is saved in external storage. */
    private fun isExternalStoreDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.externalstorage.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Check whether this document is provided by DownloadsProvider. return true means this file is a downloaded file. */
    private fun isDownloadDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.downloads.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*Check if MediaProvider provides this document, if true means this image is created in the android media app.*/
    private fun isMediaDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.android.providers.media.documents" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /*Check whether google photos provide this document, if true means this image is created in the google photos app.*/
    private fun isGooglePhotoDoc(uriAuthority: String?): Boolean {
        var ret = false
        if ("com.google.android.apps.photos.content" == uriAuthority) {
            ret = true
        }
        return ret
    }

    /* Return uri represented document file real local path.*/
    private fun getImageRealPath(
        contentResolver: ContentResolver,
        uri: Uri,
        whereClause: String?
    ): String? {
        var ret = ""

        // Query the URI with the condition.
        val cursor = contentResolver.query(uri, null, whereClause, null, null)
        if (cursor != null) {
            val moveToFirst = cursor.moveToFirst()
            if (moveToFirst) {

                // Get columns name by URI type.
                var columnName = MediaStore.Images.Media.DATA
                if (uri === MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA
                } else if (uri === MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA
                } else if (uri === MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA
                }

                // Get column index.
                val imageColumnIndex = cursor.getColumnIndex(columnName)

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex)
            }
        }
        return ret
    }
}
