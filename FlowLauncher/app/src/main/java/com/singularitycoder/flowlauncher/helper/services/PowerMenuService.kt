/* Copyright 2016 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.singularitycoder.flowlauncher.helper.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.singularitycoder.flowlauncher.helper.constants.IntentAction
import com.singularitycoder.flowlauncher.helper.constants.IntentExtra
import com.singularitycoder.flowlauncher.helper.registerCustomReceiver
import com.singularitycoder.flowlauncher.helper.showToast
import com.singularitycoder.flowlauncher.helper.unregisterCustomReceiver

// https://github.com/farmerbb/Taskbar
class PowerMenuService : AccessibilityService() {
    private val powerMenuReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!performGlobalAction(intent.getIntExtra(IntentExtra.EXTRA_ACTION, -1))) {
                this@PowerMenuService.showToast("Not supported on this device")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    override fun onCreate() {
        super.onCreate()
        registerCustomReceiver(powerMenuReceiver, IntentAction.ACTION_ACCESSIBILITY_ACTION)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCustomReceiver(powerMenuReceiver)
    }
}