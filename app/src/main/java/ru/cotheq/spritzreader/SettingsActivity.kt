package ru.cotheq.spritzreader

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SettingsActivity : AppCompatActivity() {
    private var newWPM = WPM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_settings)

        val wpmText = findViewById(R.id.wpmText) as EditText
        wpmText.setText(WPM.toString())

        val saveSettingsButton = findViewById(R.id.saveSettingsButton) as Button
        saveSettingsButton.setOnClickListener({_ ->
            val i = Intent()
            newWPM = if (wpmText.text.isNotEmpty()) wpmText.text.toString().toInt() else WPM
            if (newWPM < 50) newWPM = 50
            if (newWPM > 1000) newWPM = 1000
            i.putExtra("speed", newWPM)
            setResult(Activity.RESULT_OK, i)
            finish()
        })



    }


}

