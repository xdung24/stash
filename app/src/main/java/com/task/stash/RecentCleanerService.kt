package com.task.stash

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.Locale
import kotlin.math.max

class RecentCleanerService : AccessibilityService() {
    private val TAG = "MyAccessibilityService"

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        try {
            Log.d(TAG, "onAccessibilityEvent: $event")

            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            val isQuickstepRecents = (packageName == "com.sec.android.app.launcher" || packageName == "com.android.systemui") && className == "com.android.quickstep.RecentsActivity"
            if (isQuickstepRecents) {
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        val rootNode = rootInActiveWindow ?: return@postDelayed

                        val variants = try {
                            resources.getStringArray(R.array.close_all_variants).toList()
                        } catch (e: Exception) {
                            listOf("Close all")
                        }

                        var acted = false
                        for (v in variants) {
                            val nodes = rootNode.findAccessibilityNodeInfosByText(v)
                            if (nodes != null && nodes.isNotEmpty()) {
                                nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                acted = true
                                break
                            }
                        }

                        if (!acted) {
                            val idNodes = rootNode.findAccessibilityNodeInfosByViewId("com.android.systemui:id/button_clear_all")
                            if (idNodes != null && idNodes.isNotEmpty()) {
                                idNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                acted = true
                            }
                        }
                    } catch (inner: Exception) {
                        Log.e(TAG, "onAccessibilityEvent inner error", inner)
                    }
                }, 50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onAccessibilityEvent error", e)
        }
    }
    override fun onInterrupt() {
        Log.e(TAG, "onInterrupt: Service was interrupted.")
    }

    // MIUI helper: attempt to click MIUI's clear area (kept for future use, not invoked by default)
    private fun performMiuiClear(root: AccessibilityNodeInfo): Boolean {
        try {
            // Prefer explicit view id
            try {
                val miuiClear = root.findAccessibilityNodeInfosByViewId("com.mi.android.globallauncher:id/clearAnimView")
                if (miuiClear != null && miuiClear.isNotEmpty()) {
                    miuiClear[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            } catch (_: Exception) {}

            // Fallback: search for clickable nodes with 'clear' in view id or contentDescription
            val queue = ArrayDeque<AccessibilityNodeInfo>()
            queue.add(root)
            while (queue.isNotEmpty()) {
                val n = queue.removeFirst()
                try {
                    val vid = n.viewIdResourceName?.lowercase(Locale.getDefault())
                    val desc = n.contentDescription?.toString()
                    if (!vid.isNullOrEmpty() && vid.contains("clear")) {
                        if (n.isClickable) { n.performAction(AccessibilityNodeInfo.ACTION_CLICK); return true }
                    }
                    if (!desc.isNullOrEmpty() && desc.lowercase(Locale.getDefault()).contains("clear")) {
                        if (n.isClickable) { n.performAction(AccessibilityNodeInfo.ACTION_CLICK); return true }
                    }
                } catch (_: Exception) {
                }
                val childCount = n.childCount
                for (i in 0 until childCount) {
                    try { n.getChild(i)?.let { queue.add(it) } } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
        }
        return false
    }

    private fun isLikelyMiuiRecents(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var appLikeCount = 0
        while (queue.isNotEmpty()) {
            val n = queue.removeFirst()
            try {
                val vid = n.viewIdResourceName
                val cls = n.className?.toString()?.lowercase(Locale.getDefault())
                val desc = n.contentDescription?.toString()
                val txt = n.text?.toString()

                if (!vid.isNullOrEmpty()) {
                    val low = vid.lowercase()
                    if (low.contains("recents") || low.contains("overview") || low.contains("recent") || low.contains("task") || low.contains("card")) return true
                    // MIUI-specific IDs
                    if (low.contains("memoryandclearcontainer") || low.contains("clearanimview") || low.contains("recents_view")) return true
                }
                if (!cls.isNullOrEmpty()) {
                    if (cls.contains("recents") || cls.contains("overview") || cls.contains("recentsactivity")) return true
                }
                if (!desc.isNullOrEmpty()) {
                    if (desc.length > 1 && !desc.contains("launcher", ignoreCase = true)) appLikeCount++
                }
                if (!txt.isNullOrEmpty()) {
                    if (txt.length > 1) appLikeCount++
                }
            } catch (_: Exception) {
            }
            val childCount = max(0, n.childCount)
            for (i in 0 until childCount) {
                try { n.getChild(i)?.let { queue.add(it) } } catch (_: Exception) {}
            }
        }
        return appLikeCount >= 3
    }
}