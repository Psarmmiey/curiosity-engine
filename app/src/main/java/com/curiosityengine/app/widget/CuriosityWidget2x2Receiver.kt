package com.curiosityengine.app.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the 2×2 home screen widget variant.
 * Uses the same [CuriosityWidget] implementation as the 4×2 receiver;
 * Glance selects the appropriate content based on the available widget size.
 */
class CuriosityWidget2x2Receiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CuriosityWidget()
}
