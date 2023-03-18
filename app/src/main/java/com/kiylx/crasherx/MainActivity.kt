package com.kiylx.crasherx

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.kiylx.crashertools.CrashHelper
import com.kiylx.crashertools.CrashHelper.configCrashHandler
import com.kiylx.crashertools.CrashHelper.registerListener
import com.kiylx.crashertools.OnCrashListener

class MainActivity() : AppCompatActivity(), OnCrashListener {
    private lateinit var stackOverflowSwitch: SwitchCompat
    private lateinit var crashActivitySwitch: SwitchCompat
    private lateinit var backgroundSwitch: SwitchCompat
    private lateinit var colorButton: AppCompatButton
    private var color = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerListener(this)
        stackOverflowSwitch = findViewById(R.id.stackOverflow)
        crashActivitySwitch = findViewById(R.id.crashActivity)
        backgroundSwitch = findViewById(R.id.background)
        colorButton = findViewById(R.id.color)
        findViewById<View>(R.id.nullPointer).setOnClickListener(
            View.OnClickListener { (null as TextView?)!!.text = "Hi!" })
        stackOverflowSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            configCrashHandler {
                this.isStackOverflow = isChecked
            }
        }
        //是否跳转activity
        crashActivitySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            configCrashHandler {
                this.isCrashActivity = isChecked
            }
        }
        backgroundSwitch.setOnCheckedChangeListener { compoundButton, b ->
            configCrashHandler {
                this.isBackground = b
            }
        }
        setColor(ContextCompat.getColor(this, R.color.colorPrimary))
        colorButton.setOnClickListener {
            color++
            when (color % 5) {
                0 -> setColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                1 -> setColor(Color.parseColor("#009688"))
                2 -> setColor(Color.parseColor("#43A047"))
                3 -> setColor(Color.parseColor("#FF5722"))
                4 -> setColor(Color.parseColor("#F44336"))
            }
        }
    }

    private fun setColor(color: Int) {
        colorButton.backgroundTintList = ColorStateList.valueOf(color)
        configCrashHandler {
            this.color = color
        }
    }

    override fun onCrash(thread: Thread, throwable: Throwable) {
        Log.d("MainActivity", "Exception thrown: " + throwable.javaClass.name)
    }
}