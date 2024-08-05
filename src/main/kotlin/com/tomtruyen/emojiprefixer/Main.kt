package com.tomtruyen.emojiprefixer

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.tomtruyen.emojiprefixer.extensions.emoji
import com.tomtruyen.emojiprefixer.extensions.filterWriteable
import com.tomtruyen.emojiprefixer.manager.CalendarManager
import com.tomtruyen.emojiprefixer.utils.CalendarAuthorizationMailer
import com.tomtruyen.emojiprefixer.utils.Logger

private const val APPLICATION_NAME = "Emoji Prefixer"
private const val CALENDAR_LOOP_DELAY = 10_000L

fun main() {
    try {
        Logger.info("Starting $APPLICATION_NAME...")

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

            // Sleep for 10 seconds to prevent rate limiting
            // Most of the time this won't make much of a difference since people don't have that many calendars
            Thread.sleep(CALENDAR_LOOP_DELAY)
        }

        Logger.success("All calendars have been updated successfully")
    } catch (e: GoogleJsonResponseException) {
        if(e.details.code == 401) {
            Logger.error("Authorization error occurred. Please reauthorize the application.")

            CalendarAuthorizationMailer.send()

            return
        }
    } catch (e: Exception) {
        Logger.error("An unexpected error occurred: ${e.message}")
    }
}