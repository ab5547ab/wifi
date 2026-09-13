package com.example.whatswifiswitch

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Watches for window-state-changed events. When the foreground app switches
 * to WhatsApp we turn WiFi on and connect; when it switches away we turn
 * WiFi off. System UI packages (status bar, notification shade, etc.) are
 * ignored so a pulled-down notification shade doesn't count as "leaving"
 * WhatsApp.
 */
class WhatsAppAccessibilityService : AccessibilityService() {

    private val whatsAppPackages = setOf("com.whatsapp", "com.whatsapp.w4b")
    private val ignoredPackages = setOf(
        "com.android.systemui",
        "com.example.whatswifiswitch"
    )

    private var whatsAppIsForeground = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (packageName in ignoredPackages) return

        val isWhatsApp = packageName in whatsAppPackages

        if (isWhatsApp && !whatsAppIsForeground) {
            whatsAppIsForeground = true
            onWhatsAppOpened()
        } else if (!isWhatsApp && whatsAppIsForeground) {
            whatsAppIsForeground = false
            onWhatsAppClosed()
        }
    }

    private fun onWhatsAppOpened() {
        val networks = NetworkStore.getAll(applicationContext)
        if (networks.isNotEmpty()) {
            WifiController.turnOnAndConnect(applicationContext, networks)
        }
    }

    private fun onWhatsAppClosed() {
        WifiController.turnOff(applicationContext)
    }

    override fun onInterrupt() {
        // No-op
    }
}
