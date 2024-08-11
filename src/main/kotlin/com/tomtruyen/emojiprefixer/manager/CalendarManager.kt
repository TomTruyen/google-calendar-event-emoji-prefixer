package com.tomtruyen.emojiprefixer.manager

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.auth.oauth2.StoredCredential.DEFAULT_DATA_STORE_ID
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
import com.tomtruyen.emojiprefixer.extensions.hasCalendarEmojiPrefix
import com.tomtruyen.emojiprefixer.extensions.hasEmojiPrefix
import java.io.File
import java.io.InputStreamReader
import com.tomtruyen.emojiprefixer.utils.Logger
import com.tomtruyen.emojiprefixer.utils.PropertiesReader

class CalendarManager {
    private val properties by lazy { PropertiesReader.loadProperties() }

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
                // Ignore events that have an empty or null summary
                !it.summary.isNullOrBlank()
                        // Ignore events that already have the emoji prefix
                        && !it.hasCalendarEmojiPrefix(emoji)
                        // Ignore events that already starts with any emoji prefix since we don't want to add multiple emojis
                        // Sometimes a user might add an emoji manually to the event which is different from the calendar emoji
                        && !it.hasEmojiPrefix()
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
        val credentialsPath = properties.getProperty("CREDENTIALS_PATH")
        // Load the credentials file
        Logger.info("Loading credentials in directory: $credentialsPath")

        val inputStream = CalendarManager::class.java.getResourceAsStream(credentialsPath) ?: throw Exception("Resource not found: $credentialsPath")

        // Load the client secrets
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        val tokenPath = properties.getProperty("TOKEN_PATH")

        val dataStoreFactory = FileDataStoreFactory(File(tokenPath))

        Logger.info("Storing tokens in directory: $tokenPath")

        // Create a new flow
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(dataStoreFactory)
            .setApprovalPrompt("force")
            .setAccessType("offline")
            .build()

        val credential = flow.loadCredential("user")

        val hasRefreshedToken = refreshOAuthCredential(credential)

        if(hasRefreshedToken) {
            Logger.info("Successfully refreshed OAuth credential")

            Logger.info("Saving refreshed OAuth credential...")

            dataStoreFactory.getDataStore<StoredCredential>(DEFAULT_DATA_STORE_ID).set("user", StoredCredential(credential))

            Logger.info("Successfully saved refreshed OAuth credential")

            return credential
        }

        // Authorize the flow
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    private fun refreshOAuthCredential(credential: Credential?): Boolean {
        Logger.info("Refreshing OAuth credential...")

        return credential?.refreshToken() ?: false
    }

    companion object {
        private const val APPLICATION_NAME = "Emoji Prefixer"

        private val JSON_FACTORY = GsonFactory.getDefaultInstance()

        private val SCOPES = listOf(CalendarScopes.CALENDAR, CalendarScopes.CALENDAR_EVENTS)
    }
}