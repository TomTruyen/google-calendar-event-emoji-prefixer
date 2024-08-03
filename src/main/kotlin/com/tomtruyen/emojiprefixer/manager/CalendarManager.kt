package com.tomtruyen.emojiprefixer.manager

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import java.io.File
import java.io.InputStreamReader
import com.tomtruyen.emojiprefixer.utils.Logger

class CalendarManager {
    private val httpTransport by lazy { GoogleNetHttpTransport.newTrustedTransport() }

    // Create a new instance of the Calendar service
    private fun getCalendarService(credential: Credential) = Calendar
        .Builder(httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build()


    fun fetchCalendars(credential: Credential): List<CalendarListEntry> {
        val service = getCalendarService(credential)

        // Fetch all calendars
        return service.calendarList().list().execute().items.orEmpty()
    }

    fun addEmojiToEvents(credential: Credential, calendarId: String, emoji: String) {
        // Create a list of events that need to be updated
        val events = fetchEventsByCalendarId(credential, calendarId)
            .filter {
                // Ignore events that have an empty or null summary or already have an emoji prefix
                !it.summary.isNullOrBlank() && !it.summary.trim().startsWith(emoji)
            }.map {
                // Add the emoji prefix to the summary
                it.apply {
                    summary = "$emoji ${it.summary.trim()}"
                }
            }

        // Batch update the events
        val service = getCalendarService(credential)

        // Create a batch request
        val batch = service.batch()

        val callback = object: JsonBatchCallback<Event>() {
            override fun onSuccess(event: Event?, headers: HttpHeaders?) {
                Logger.success("Batch request successful for calendar: $calendarId with event: ${event?.id}")
            }

            override fun onFailure(error: GoogleJsonError?, headers: HttpHeaders?) {
                Logger.error("Batch request failed: ${error?.message} for calendar: $calendarId")
            }
        }

        // Queue the events to be updated
        events.forEach { event ->
            service.events().update(calendarId, event.id, event).queue(batch, callback)
        }

        // Execute the batch request
        if(batch.size() > 0) {
            Logger.info("Executing batch request for calendar: $calendarId with ${events.size} events")
            batch.execute()
        }
    }

    private fun fetchEventsByCalendarId(credential: Credential, calendarId: String): List<Event> {
        val service = getCalendarService(credential)

        val now = DateTime(System.currentTimeMillis())
        val future = DateTime(System.currentTimeMillis() + FETCH_EVENTS_DAYS * 24 * 60 * 60 * 1000)

        // Fetch all events from the calendar
        // Only fetch events that are between now and [FETCH_EVENTS_DAYS] days in the future
        return service.events().list(calendarId)
            .setTimeZone("Europe/Brussels")
            .setTimeMin(now)
            .execute()
            .items
            .orEmpty()
    }

    // TODO: Future improvement
    //  Implement a way to fetch the OAuth credential without the need of a GUI
    //  or by being able to opening the link and authorizing on a different machine that has GUI support
    fun fetchOAuthCredential(): Credential {
        // Load the credentials file
        val inputStream = CalendarManager::class.java.getResourceAsStream(PATH_CREDENTIALS_FILE) ?: throw Exception("Resource not found: $PATH_CREDENTIALS_FILE")

        // Load the client secrets
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // Create a new flow
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(PATH_TOKENS_DIRECTORY)))
            .setAccessType("offline")
            .build()

        // Authorize the flow
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        val credential = AuthorizationCodeInstalledApp(flow, receiver).authorize("user")

        return credential
    }

    companion object {
        private const val APPLICATION_NAME = "Emoji Prefixer"

        private const val PATH_TOKENS_DIRECTORY = "tokens"
        private const val PATH_CREDENTIALS_FILE = "/credentials.json"

        private val JSON_FACTORY = GsonFactory.getDefaultInstance()

        private val SCOPES = listOf(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS)

        private const val FETCH_EVENTS_DAYS = 30L
    }
}