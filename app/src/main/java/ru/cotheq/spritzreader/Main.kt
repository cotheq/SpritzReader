package ru.cotheq.spritzreader

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.widget.RelativeLayout.LayoutParams
import kotlin.collections.ArrayList
import android.view.MotionEvent
import android.widget.*
import kotlinx.android.synthetic.main.a_main.*
import java.io.File

class Main : AppCompatActivity() {
    val LOAD_TEXT = 1
    val OPEN_SETTINGS = 2

    private lateinit var spritzLayout: RelativeLayout
    private lateinit var redLetter: TextView
    private lateinit var leftPart: TextView
    private lateinit var rightPart: TextView
    private lateinit var upperText: TextView
    private lateinit var lowerText: TextView
    private lateinit var remainder: TextView
    private lateinit var upperScroll: ScrollView
    private lateinit var lowerScroll: ScrollView
    private lateinit var prevParagraph: ImageButton
    private lateinit var prevWord: ImageButton
    private lateinit var nextParagraph: ImageButton
    private lateinit var nextWord: ImageButton
    private lateinit var loadText: ImageButton
    private lateinit var settingsButton: ImageButton

    private val handler = Handler()
    private var wordIndex = 0
    //private val s = "Быстрое последовательное визуальное предъявление — способ показа текстовой информации на дисплее (англ. Rapid Serial Visual Presentation, RSVP). RSVP – сегодня самая распространенная техника скорочтения. Однако, RSVP была изначально разработана для проведения психологического эксперимента, имевшего целью определить реакцию человека на содержание читаемого текста. В то время, когда была изобретена техника RSVP, цифрового контента в мире было не так уж много, в любом случае, большинство людей не имело к нему доступа. Еще даже не существовало интернета. В традиционной версии RSVP слова выравниваются либо по левому краю, либо по центру.\n\n Быстрое последовательное визуальное предъявление — способ показа текстовой информации на дисплее (англ. Rapid Serial Visual Presentation, RSVP). RSVP – сегодня самая распространенная техника скорочтения. Однако, RSVP была изначально разработана для проведения психологического эксперимента, имевшего целью определить реакцию человека на содержание читаемого текста. В то время, когда была изобретена техника RSVP, цифрового контента в мире было не так уж много, в любом случае, большинство людей не имело к нему доступа. Еще даже не существовало интернета. В традиционной версии RSVP слова выравниваются либо по левому краю, либо по центру."

    //just for init
    private var loadedText = "Spritz Reader".split(" ") as ArrayList<String>
    private var wordArray = splitToWordArray(loadedText)

    private var wait = 1.0
    private var isReading = false
    private var wordPartIndex = 0
    private val readerRunnable = Runnable { startReading() }
    private var prevY = 0F
    private var fileOpened = false

    fun splitToWordArray(s: String): ArrayList<String> {
        var paragraphs = s.split('\n') as ArrayList<String>
        var wordArray = ArrayList<String>()
        for (paragraph in paragraphs) {
            if (paragraph.isNotEmpty()) {
                if (paragraph.indexOf(' ') >= 0) {
                    val words = paragraph.split(' ')
                    for (word in words)
                        if (word.isNotEmpty())
                            wordArray.add(word + (if (word == words.last()) '\n' else ""))
                } else {
                    wordArray.add(paragraph)
                }
            }
        }

        if (wordArray.size == 0) {
            wordArray.add("")
        }

        return wordArray
    }

    fun splitToWordArray(paragraphs: ArrayList<String>): ArrayList<String> {
        var wordArray = ArrayList<String>()
        for (paragraph in paragraphs) {
            if (paragraph.isNotEmpty()) {
                if (paragraph.indexOf(' ') >= 0) {
                    val words = paragraph.split(' ')
                    for (word in words)
                        if (word.isNotEmpty())
                            wordArray.add(word + (if (word == words.last()) '\n' else ""))
                } else {
                    wordArray.add(paragraph)
                }
            }
        }

        if (wordArray.size == 0) {
            wordArray.add("")
        }

        return wordArray
    }


    fun getTextWidth(textView: TextView): Int {
        textView.measure(0, 0)
        return textView.measuredWidth
    }

    fun alignSpritzWord() {
        val displayWidth = resources.displayMetrics.widthPixels
        val lWidth = getTextWidth(leftPart)
        val cWidth = getTextWidth(redLetter)
        val rWidth = getTextWidth(rightPart)
        val cx = (displayWidth * SPRITZ_FOCUS - cWidth / 2).toInt()
        val lx = cx - lWidth
        val rx = cx + cWidth
        val wordTopMargin = spritzLayout.layoutParams.height / 2 - leftPart.lineHeight / 2

        var params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = lx
        params.topMargin = wordTopMargin
        leftPart.layoutParams = params

        params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = cx
        params.topMargin = wordTopMargin
        redLetter.layoutParams = params

        params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = rx
        params.topMargin = wordTopMargin
        rightPart.layoutParams = params

        params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = rx + rWidth
        params.topMargin = wordTopMargin
        remainder.layoutParams = params

    }

    fun setWord(_word: String, leaveRemainder: Boolean = false, getWaitOnly: Boolean = false): Double {
        var wait = 1.0
        var word = _word
        var remainderText = ""

        if (word.isNotEmpty()) {
            var wordLength = word.length

            wait = when (word.last()) {
                ',', ':', ';', '-' -> 1.75
                '!', '?' -> 2.0
                '.' -> 3.0
                '\n' -> 4.0
                else -> 1.0
            }

            if (word.indexOf('\n') >= 0) {
                word = word.replace("\n", "")
                wordLength--
            }

            if ( (wordLength > 1) && (word.last() in listOf('.', ',', '!', '?', ':', ';', '-')) )
                wordLength--

            //if (wordLength < 5) wait -= 0.1
            if (wordLength > WORD_PART_LENGTH) wait += 0.25
            if (wordLength > LONG_WORD_LENGTH) wait += 0.5

            if (!getWaitOnly) {
                val redLetterIndex = (wordLength + 6) / 4 - 1
                val red = Character.toString(word[redLetterIndex])
                val leftWordPart = word.substring(0, redLetterIndex)
                val rightWordPart = word.substring(redLetterIndex + 1)

                leftPart.text = leftWordPart
                redLetter.text = red
                rightPart.text = rightWordPart

                if (!isReading) {
                    var upperTextArray = ArrayList<String>()
                    for (i in wordIndex - WORDS_VIEW_BUFFER..wordIndex - 1) {
                        if (i >= 0) {
                            upperTextArray.add(wordArray[i])
                        }
                    }

                    var lowerTextArray = ArrayList<String>()
                    for (i in wordIndex + 1..wordIndex + WORDS_VIEW_BUFFER)
                        if (i <= wordArray.size - 1)
                            lowerTextArray.add(wordArray[i])

                    upperText.text = upperTextArray.joinToString(" ")
                    lowerText.text = lowerTextArray.joinToString(" ")
                } else {
                    for (i in wordIndex + 1..wordIndex + 5)
                        if (i <= wordArray.size - 1)
                            remainderText += " ${wordArray[i]}"
                }

                if (isReading) {
                    var remainderWord = ""
                    if ((word.length <= wordArray[wordIndex].length) && (leaveRemainder) && (word.length == WORD_PART_LENGTH)) {
                        remainderWord = wordArray[wordIndex].substring(word.length * (wordPartIndex + 1), wordArray[wordIndex].length)
                        wait += 0.5
                    }
                    remainder.text = """$remainderWord $remainderText"""
                }

                alignSpritzWord()
            }
        } else {
            if (!getWaitOnly) {
                leftPart.text = ""
                rightPart.text = ""
                redLetter.text = ""
                remainder.text = ""
            }
        }

        return wait
    }

    fun setVisibleEnabledForReading(isReading: Boolean) = if (isReading) {
        remainder.visibility = View.VISIBLE
        upperText.visibility = View.INVISIBLE
        lowerText.visibility = View.INVISIBLE
        buttonsLayout.visibility = View.INVISIBLE
    } else {
        remainder.visibility = View.INVISIBLE
        upperText.visibility = View.VISIBLE
        lowerText.visibility = View.VISIBLE
        buttonsLayout.visibility = View.VISIBLE
    }

    fun readNextWord() {

        if (wordIndex >= wordArray.size - 1) {
            stopReading()
            return
        }


        if (wordArray[wordIndex] in listOf("", " ", "\n")) {
            wordArray.removeAt(wordIndex)
        }

        var forwardCount = 0
        var waitCorrector = 0.0
        for (i in wordIndex .. wordIndex + 99) {
            if (i <= wordArray.size - 1) {
                forwardCount++
                waitCorrector += setWord(wordArray[i], getWaitOnly = true)
            }
        }

        val timeout = if (wordIndex < wordArray.size - 5) (BASIC_TIMEOUT * wait / (waitCorrector / forwardCount)) else (BASIC_TIMEOUT * wait)
        /*val yyy = findViewById(R.id.yyy) as TextView
        yyy.text = timeout.toString() + " " + BASIC_TIMEOUT + " " + waitCorrector.toString() + " " + forwardCount*/

        var newWord = wordArray[wordIndex]
        var leaveRemainder = false
        if (newWord.isNotEmpty()) {
            var wordPartArray = ArrayList<String>()
            if (newWord.length < LONG_WORD_LENGTH) {
                wordPartArray.add(newWord)
                leaveRemainder = false
            } else {
                leaveRemainder = true
                var letterIndex = 0
                while (letterIndex <= newWord.length - 1) {
                    var wordPart = ""
                    wordPart = newWord.substring(letterIndex, letterIndex + WORD_PART_LENGTH)
                    if (letterIndex + WORD_PART_LENGTH * 2  >= newWord.length) {
                        wordPart += newWord.substring(letterIndex + WORD_PART_LENGTH, newWord.length)
                        letterIndex += WORD_PART_LENGTH * 2
                    } else
                        letterIndex += WORD_PART_LENGTH
                    wordPartArray.add(wordPart)
                }
            }

            when(newWord[newWord.length - 1]) {
                in listOf('.', '!', '?', '\n') -> wordPartArray.add("")
            }

            if (wordPartIndex == wordPartArray.size - 1)
                leaveRemainder = false

            wait = setWord(
                    if (wordPartArray.size == 0)
                        wordPartArray[0]
                    else {
                        wordPartArray[wordPartIndex]
                    },
                    leaveRemainder)

            wordPartIndex++
            if (wordPartIndex >= wordPartArray.size ) {
                wordPartIndex = 0
                wordPartArray.clear()
                leaveRemainder = false
                wordIndex++
            }

        } else {
            wordIndex++
        }

        handler.postDelayed(readerRunnable, timeout.toLong())
    }

    fun startReading() {
        if (!isReading) {
            isReading = true
            setVisibleEnabledForReading(true)
            if (wordIndex >= wordArray.size - 1)
                wordIndex = 0
            setWord(wordArray[wordIndex])
            handler.postDelayed(readerRunnable, FIRST_DELAY)
        } else {
            readNextWord()
        }
    }

    fun stopReading() {
        isReading = false
        handler.removeCallbacksAndMessages(null)
        setVisibleEnabledForReading(false)
        wordPartIndex = 0
        if (wordIndex > 0)
            setWord(wordArray[wordIndex])
    }

    fun jumpToPrevWord() {
        wordIndex--
        if (wordIndex < 0) wordIndex = 0
        setWord(wordArray[wordIndex])
    }

    fun jumpToNextWord() {
        wordIndex++
        if (wordIndex >= wordArray.size - 1) wordIndex = wordArray.size - 1
        setWord(wordArray[wordIndex])
    }

    fun jumpToPrevParagraph() {
        if (wordIndex > 0) {
            if (wordArray[wordIndex - 1].indexOf('\n') >= 0) {
                wordIndex--
            }
            do {
                wordIndex--
            } while ((wordIndex > 0) && (wordArray[wordIndex].indexOf('\n') < 0))

            if ( (wordArray[wordIndex].indexOf('\n') >= 0) || (wordIndex != 0) )
                wordIndex++
        } else {
            wordIndex = 0
        }
        setWord(wordArray[wordIndex])
    }

    fun jumpToNextParagraph() {
        if (wordIndex < wordArray.size - 1) {

            if (wordIndex < wordArray.size - 2)
                if (wordArray[wordIndex + 1].indexOf('\n') >= 0)
                    wordIndex++

            do {
                wordIndex++
            } while ((wordIndex < wordArray.size) && (wordArray[wordIndex].indexOf('\n') < 0))

            if (wordArray[wordIndex].indexOf('\n') >= 0)
                wordIndex++

            if (wordIndex >= wordArray.size)
                wordIndex = wordArray.size - 1
        } else {
            wordIndex = wordArray.size - 1
        }
            setWord(wordArray[wordIndex])
    }

    fun loadSettings(settingsFile: File) {
        val settings = settingsFile.readLines()[0].split('|')
        setWPM(settings[0].toInt())
        wordIndex = settings[1].toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main)

        val loadTextIntent = Intent(this, StartActivity::class.java)
        val settingsIntent = Intent(this, SettingsActivity::class.java)

        //init variables
        spritzLayout = findViewById(R.id.spritzLayout) as RelativeLayout
        redLetter = findViewById(R.id.redLetter) as TextView
        leftPart = findViewById(R.id.leftPart) as TextView
        rightPart = findViewById(R.id.rightPart) as TextView
        upperText = findViewById(R.id.upperText) as TextView
        lowerText = findViewById(R.id.lowerText) as TextView
        remainder = findViewById(R.id.remainderTextView) as TextView
        upperScroll = findViewById(R.id.upperScroll) as ScrollView
        lowerScroll = findViewById(R.id.lowerScroll) as ScrollView
        prevParagraph = findViewById(R.id.prevParagraph) as ImageButton
        prevWord = findViewById(R.id.prevWord) as ImageButton
        nextParagraph = findViewById(R.id.nextParagraph) as ImageButton
        nextWord = findViewById(R.id.nextWord) as ImageButton
        loadText = findViewById(R.id.loadText) as ImageButton
        settingsButton = findViewById(R.id.settingsButton) as ImageButton

        prevWord.setOnClickListener({ _ -> jumpToPrevWord() })
        nextWord.setOnClickListener({_ -> jumpToNextWord() })
        prevParagraph.setOnClickListener({_ -> jumpToPrevParagraph() })
        nextParagraph.setOnClickListener({_ -> jumpToNextParagraph() })
        loadText.setOnClickListener({_ -> startActivityForResult(loadTextIntent, LOAD_TEXT)})
        settingsButton.setOnClickListener({_ -> startActivityForResult(settingsIntent, OPEN_SETTINGS)})


        //add focus lines view to spritz layout
        val focusLines = DrawFocusLines(this, SPRITZ_FOCUS)
        spritzLayout.addView(focusLines)
        //spritzLayout.removeViewAt(4)

        //add event listeners
        upperScroll.setOnTouchListener({ _, event -> this.onTouchEvent(event); true })
        lowerScroll.setOnTouchListener({ _, event -> this.onTouchEvent(event); true })
        spritzLayout.setOnTouchListener ({ _, event -> false })

        spritzLayout.setOnClickListener ({ _ -> if (!isReading) startReading() else stopReading() })

        //loading settings from file
        val settingsFile = File(filesDir, "settings")
        if (settingsFile.exists()) {
            loadSettings(settingsFile)
            val savedTextFile = File(filesDir, "savedtext")
            loadedText = savedTextFile.readLines() as ArrayList<String>
            wordArray = splitToWordArray(loadedText)
            fileOpened = true
        } else {
            startActivityForResult(loadTextIntent, LOAD_TEXT)
        }

        setVisibleEnabledForReading(false)
        setWord(wordArray[wordIndex])
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if ( (event != null) && (!isReading) ) {
            var y = event.y
            var dy = 0F
            if (event.action == MotionEvent.ACTION_MOVE)
                dy = (y - prevY)
            wordIndex = (wordIndex - (dy / 6).toInt())
            if (wordIndex < 0) wordIndex = 0
            if (wordIndex >= wordArray.size) wordIndex = wordArray.size - 1

            setWord(wordArray[wordIndex])
            prevY = y
            return true
        } else {
            return false
        }
    }

    fun saveSettings() {
        val settingsFile = File(filesDir, "settings")
        settingsFile.createNewFile()
        val _WPM = WPM
        settingsFile.writeText("""$_WPM|$wordIndex""")
    }

    fun saveText() {
        val savedTextFile = File(filesDir, "savedtext")
        savedTextFile.createNewFile()
        savedTextFile.writeText(wordArray.joinToString(" "))
    }

    override fun onPause() {
        super.onPause()
        stopReading()

        //save settings to file
        if (fileOpened) {
            saveSettings()
            saveText()
        }
    }

    override fun onBackPressed() {
        if (isReading) stopReading() else super.onBackPressed()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null) {
            when (requestCode) {
                LOAD_TEXT -> if ((resultCode == Activity.RESULT_OK) ) {
                    loadedText = intent.getStringArrayListExtra("text")
                    wordArray = splitToWordArray(loadedText)
                    wordIndex = 0
                    wait = setWord(wordArray[wordIndex])
                    fileOpened = true
                }
                OPEN_SETTINGS -> if (resultCode == Activity.RESULT_OK) {
                    val newWPM = intent.getIntExtra("speed", WPM)
                    setWPM(newWPM)
                }
            }
        } else {
            if (!fileOpened) {
                finish()
            }
        }
    }

    fun setWPM(newWPM: Int) {
        WPM = newWPM
        BASIC_TIMEOUT = 60000F / WPM
    }
}

