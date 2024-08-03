package com.tomtruyen.emojiprefixer.extensions

import com.google.api.services.calendar.model.CalendarListEntry

fun List<CalendarListEntry>.filterWriteable() = filter {
    it.accessRole.equals("owner", ignoreCase = true) || it.accessRole.equals("writer", ignoreCase = true)
}

fun CalendarListEntry.emoji(divider: String = " "): String? {
    if(summary.trim().contains(divider)) {
        return summary.trim().split(divider)[0]
    }

    return null
}