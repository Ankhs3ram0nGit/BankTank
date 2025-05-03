package com.example.expensetracker.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BMLAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("BMLService", "Event received: ${event?.eventType} from ${event?.packageName}")

        // Check for TYPE_WINDOW_CONTENT_CHANGED (content change in window)
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event?.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {

            val packageName = event?.packageName?.toString() ?: return
            Log.d("BMLService", "App opened: $packageName")

            // If we're in the BML app and content has changed
            if (packageName == "mv.com.bml.mib") {
                val rootNode = rootInActiveWindow ?: return
                if (isReceiptScreen(rootNode)) {
                    Log.d("BMLService", "Receipt screen detected.")

                    // Check if permission to draw overlays is granted
                    if (!Settings.canDrawOverlays(this)) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + this.packageName))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)  // Request the user to grant the permission
                    } else {
                        // Proceed to show your floating popup
                        showFloatingPopup()
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // Not used in most cases
    }

    private fun isReceiptScreen(rootNode: AccessibilityNodeInfo): Boolean {
        // Check if "Transfer successful" text is found in the root node
        val successText = rootNode.findAccessibilityNodeInfosByText("Transfer successful")
        return successText.isNotEmpty()
    }

    private fun showFloatingPopup() {
        // Get the WindowManager system service
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create a TextView to display the floating popup
        val textView = TextView(this).apply {
            text = "Transaction Detected!"
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
            textSize = 18f
            setPadding(20, 20, 20, 20)
            gravity = Gravity.CENTER
        }

        // Set up WindowManager.LayoutParams
        val params = WindowManager.LayoutParams().apply {
            // Set type to SYSTEM_ALERT to show on top of other apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                type = WindowManager.LayoutParams.TYPE_PHONE
            }

            // Set the layout properties
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER

            // Set the position of the floating view (e.g., in the middle of the screen)
            x = 0
            y = 0
        }

        // Add the TextView to the WindowManager (this will display it above other apps)
        windowManager.addView(textView, params)

        // Optionally, remove the view after a few seconds (e.g., 5 seconds)
        textView.postDelayed({
            windowManager.removeView(textView)
        }, 5000)  // 5000 ms = 5 seconds
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("MyAccessibilityService", "Service connected!")
    }
}
