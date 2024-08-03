package com.tomtruyen.emojiprefixer

import com.tomtruyen.emojiprefixer.extensions.emoji
import com.tomtruyen.emojiprefixer.extensions.filterWriteable
import com.tomtruyen.emojiprefixer.manager.CalendarManager
import com.tomtruyen.emojiprefixer.utils.Logger

fun main() {
    try {
        // Create a new instance of the CalendarManager
        val manager = CalendarManager()

        // Fetch the OAuth credential
        val credential = manager.fetchOAuthCredential()

        // Fetch all calendars that are writeable
        val calendars = manager.fetchCalendars(credential).filterWriteable()

        // Loop through all calendars
        calendars.forEach { calendar ->
            Logger.info("Checking calendar: ${calendar.summary} (${calendar.id})")

            // Fetch the emoji from the calendar
            val emoji = calendar.emoji() ?: run {
                Logger.warn("No emoji found for calendar: ${calendar.summary} (${calendar.id})")
                return@forEach
            }

            // Add the emoji to the events
            manager.addEmojiToEvents(credential, calendar.id, emoji)

            Thread.sleep(5000L)
        }
    } catch (e: Exception) {
        Logger.error("An unexpected error occurred: ${e.message}")
    }
}