package ru.cotheq.spritzreader

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.util.*

class MyTextActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var loadTextButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_mytext)

        inputText = findViewById(R.id.inputText) as EditText
        loadTextButton = findViewById(R.id.loadTextButton) as Button

        loadTextButton.setOnClickListener({_ ->
            if (inputText.text.isNotEmpty()) {
                val i = Intent()
                i.putExtra("loadedText", inputText.text.split("\n") as ArrayList<String>)
                setResult(RESULT_OK, i);
                finish()
            }
        })

    }
}
