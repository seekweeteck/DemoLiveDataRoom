package my.edu.tarc.mycontact

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import my.edu.tarc.mycontact.databinding.ActivityInsertBinding
import my.edu.tarc.mycontact.model.Contact

class InsertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInsertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSave.setOnClickListener {
            val replyIntent = Intent()
            if(TextUtils.isEmpty(binding.editTextTextPersonName2.text)){
                binding.editTextTextPersonName2.error = getString(R.string.error_value_required)
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(binding.editTextPhone2.text)){
                binding.editTextPhone2.error = getString(R.string.error_value_required)
                return@setOnClickListener
            }
            val name = binding.editTextTextPersonName2.text.toString()
            val phone = binding.editTextPhone2.text.toString()

            replyIntent.putExtra("name", name)
            replyIntent.putExtra("phone", phone)
            setResult(RESULT_OK, replyIntent)
            finish()
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }
}