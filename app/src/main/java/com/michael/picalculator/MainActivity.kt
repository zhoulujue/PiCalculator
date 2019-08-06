package com.michael.picalculator

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var editText: EditText
    private lateinit var calculatorBinder: CalculatorService.Binder
    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        setSupportActionBar(toolbar)
    }

    private fun init() {
        startButton = findViewById(R.id.start)
        pauseButton = findViewById(R.id.pause)
        stopButton = findViewById(R.id.stop)
        editText = findViewById(R.id.edit_text)

        startButton.isEnabled = true
        pauseButton.isEnabled = false
        stopButton.isEnabled = false

        var intent = Intent(this, CalculatorService::class.java)
        bindService(intent, Connection(), Context.BIND_AUTO_CREATE)

        startButton.setOnClickListener {
            disposable = calculatorBinder.start().observeOn(AndroidSchedulers.mainThread()).subscribe { editText.append(it) }

            startButton.isEnabled = false
            pauseButton.isEnabled = true
            stopButton.isEnabled = true
        }
        pauseButton.setOnClickListener {
            disposable.dispose()
            calculatorBinder.pause()

            startButton.isEnabled = true
            pauseButton.isEnabled = false
            stopButton.isEnabled = true
        }
        stopButton.setOnClickListener {
            disposable.dispose()
            calculatorBinder.stop()

            startButton.isEnabled = true
            pauseButton.isEnabled = false
            stopButton.isEnabled = false
            editText.setText("")
        }

    }

    inner class Connection: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            calculatorBinder = service as CalculatorService.Binder
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
