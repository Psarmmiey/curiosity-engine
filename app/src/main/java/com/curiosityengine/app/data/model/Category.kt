package com.curiosityengine.app.data.model

enum class Category(
    val displayName: String,
    val emoji: String,
    val color: Long
) {
    SCIENCE("Science", "🔬", 0xFF4CAF50),
    NATURE("Nature", "🌿", 0xFF8BC34A),
    HISTORY("History", "🏛️", 0xFF795548),
    PSYCHOLOGY("Psychology", "🧠", 0xFF9C27B0),
    TECHNOLOGY("Technology", "💻", 0xFF2196F3),
    ART_AND_CULTURE("Art & Culture", "🎨", 0xFFE91E63),
    ECONOMICS("Economics", "📈", 0xFFFF9800),
    PHILOSOPHY("Philosophy", "🦉", 0xFF607D8B),
    SURPRISE_ME("Surprise Me", "🎲", 0xFFFF5722);

    companion object {
        fun fromDisplayName(name: String): Category =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
                ?: SURPRISE_ME

        fun fromName(name: String): Category =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: SURPRISE_ME
    }
}
