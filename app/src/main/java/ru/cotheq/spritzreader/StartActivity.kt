package ru.cotheq.spritzreader

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button



class StartActivity : AppCompatActivity() {

    val FROM_FILE_REQUEST = 1
    val MY_TEXT_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_start)

        val fromFile = findViewById(R.id.fromFile) as Button
        fromFile.setOnClickListener({ _ ->
            val fileChooseIntent = Intent(Intent.ACTION_GET_CONTENT)
            fileChooseIntent.type = "text/*"
            startActivityForResult(Intent.createChooser(fileChooseIntent, "Open text file"), FROM_FILE_REQUEST)
        })

        val myText = findViewById(R.id.myText) as Button
        myText.setOnClickListener({ _ ->
            val inputTextIntent = Intent(this, MyTextActivity::class.java)
            startActivityForResult(inputTextIntent, MY_TEXT_REQUEST)
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            when (requestCode) {
                FROM_FILE_REQUEST -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val fileUri = intent.data
                        val loadedText = contentResolver.openInputStream(fileUri).bufferedReader().readLines() as ArrayList<String>
                        val i = Intent()
                        i.putExtra("text", loadedText)
                        setResult(RESULT_OK, i);
                        finish()
                    }
                }
                MY_TEXT_REQUEST -> {
                    if (resultCode == Activity.RESULT_OK) {
                        val loadedText = intent.getStringArrayListExtra("loadedText")
                        val i = Intent()
                        i.putExtra("text", loadedText)
                        setResult(RESULT_OK, i);
                        finish()
                    }
                }
            }
        }
    }


}
