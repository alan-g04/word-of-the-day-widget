package com.example.wordwidget

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "Word Widget is successfully installed!\n\nYou can now safely close this window and add the widget to your home screen."
            gravity = Gravity.CENTER
            textSize = 18f
            setPadding(32, 32, 32, 32)
        }
        
        setContentView(textView)
    }
}