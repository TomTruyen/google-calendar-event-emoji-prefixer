package com.tomtruyen.emojiprefixer.extensions

import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import net.fellbaum.jemoji.EmojiManager

fun List<CalendarListEntry>.filterWriteable() = filter {
    it.accessRole.equals("owner", ignoreCase = true) || it.accessRole.equals("writer", ignoreCase = true)
}

fun CalendarListEntry.emoji() = summary.emojiPrefix()

fun Event.hasCalendarEmojiPrefix(emoji: String): Boolean {
    return summary.trim().startsWith(emoji)
}

fun Event.hasEmojiPrefix(): Boolean {
    val emoji = EmojiManager.extractEmojisInOrder(summary).getOrNull(0)?.emoji ?: return false

    return summary.trim().startsWith(emoji)
}

fun String.emojiPrefix(): String? {
    val emoji = EmojiManager.extractEmojisInOrder(this).getOrNull(0)?.emoji ?: return null

    if(trim().startsWith(emoji)) {
        return emoji
    }

    return null
}