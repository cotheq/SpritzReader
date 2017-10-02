package ru.cotheq.spritzreader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class Main extends AppCompatActivity {

    private int getTextWidth(TextView textView) {
        textView.measure(0, 0);
        return textView.getMeasuredWidth();
    }

    private void alignSpritzWord(int width, TextView l, TextView c, TextView r) {
        int lWidth = getTextWidth(l);
        int cWidth = getTextWidth(c);
        int rWidth = getTextWidth(r);
        int lx, cx, rx;
        cx = (int)(width * 0.35 - cWidth / 2);
        lx = cx - lWidth;
        rx = cx + cWidth;
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(lx);
        l.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(cx);
        c.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(rx);
        r.setLayoutParams(params);
    }

    private double setWord(String word, TextView l, TextView c, TextView r) {
        double wait = 1;
        int wordLength = word.length();
        if (wordLength > 1) {
            if ((word.charAt(wordLength - 1) == '!') ||
                    (word.charAt(wordLength - 1) == '.') ||
                    (word.charAt(wordLength - 1) == ',') ||
                    (word.charAt(wordLength - 1) == '?')) {
                wordLength -= 1;
                wait = ((word.charAt(wordLength - 1)) == ',') ? 1.5 : 2;
            }
        }
        if (wordLength > 10) {
            wait += 0.5;
        }
        int redLetterIndex = (wordLength + 6) / 4 - 1;
        String red = Character.toString(word.charAt(redLetterIndex));
        String leftWordPart = word.substring(0, redLetterIndex);
        String rightWordPart = word.substring(redLetterIndex + 1);
        l.setText(leftWordPart);
        c.setText(red);
        r.setText(rightWordPart);
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        alignSpritzWord(displayWidth, l, c, r);
        return wait;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        final TextView redLetter = (TextView) findViewById(R.id.redLetter);
        final TextView leftPart = (TextView) findViewById(R.id.leftPart);
        final TextView rightPart = (TextView) findViewById(R.id.rightPart);

        final double wait = setWord("textview", leftPart, redLetter, rightPart);

        final Button textPlayButton = (Button) findViewById(R.id.textPlayButton);

        final EditText inputText = (EditText) findViewById(R.id.inputText);

        final Handler handler = new Handler();
        final Runnable timeUpdaterRunnable = new Runnable() {
            int wordIndex = 0;
            public void run() {

                String s = inputText.getText().toString();
                String[] wordArray = s.split(" ");
                String newWord = wordArray[wordIndex];
                wordIndex++;
                if (newWord.length() > 0) {
                    double wait = setWord(newWord, leftPart, redLetter, rightPart);
                    if (wordIndex < wordArray.length) {
                        handler.postDelayed(this, (long) (100 * wait));
                    } else {
                        wordIndex = 0;
                        handler.removeCallbacksAndMessages(null);
                        textPlayButton.setEnabled(true);
                    }
                } else {
                    handler.postDelayed(this, (long) (100 * wait));
                }
            }
        };

        textPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(timeUpdaterRunnable, (long)(100*wait));
                v.setEnabled(false);
            }
        });

    }
}
