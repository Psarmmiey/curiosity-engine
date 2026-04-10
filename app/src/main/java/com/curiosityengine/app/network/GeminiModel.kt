package com.curiosityengine.app.network

enum class GeminiModel(val modelId: String) {
    PRO("gemini-2.5-pro"),
    FLASH("gemini-2.5-flash"),
    FLASH_IMAGE("gemini-2.5-flash-image"),
    PRO_PREVIEW("gemini-3.1-pro-preview");

    companion object {
        val DEFAULT = PRO
        val DOOM_SCROLL = FLASH
        val IMAGE = FLASH_IMAGE
    }
}
