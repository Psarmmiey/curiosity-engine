package com.curiosityengine.app.widget

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CuriosityWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget = CuriosityWidget()
}
