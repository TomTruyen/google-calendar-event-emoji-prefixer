package com.tomtruyen.emojiprefixer.utils

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TrackOpens
import com.mailjet.client.transactional.TransactionalEmail
import java.util.Properties

object CalendarAuthorizationMailer {
    private const val PATH_ENVIRONMENT = "/local.properties"

    private const val KEY_MAILJET_API_KEY = "MAILJET_API_KEY"
    private const val KEY_MAILJET_API_SECRET = "MAILJET_SECRET_KEY"
    private const val KEY_MAILJET_TO_EMAIL = "MAILJET_TO_EMAIL"
    private const val KEY_MAILJET_FROM_EMAIL = "MAILJET_FROM_EMAIL"

    private const val PROJECT_NAME = "Emoji Prefixer"

    private val properties = Properties().apply {
        load(CalendarAuthorizationMailer::class.java.getResourceAsStream(PATH_ENVIRONMENT))
    }

    private val client by lazy {
        val apiKey = properties.getProperty(KEY_MAILJET_API_KEY)
        val apiSecret = properties.getProperty(KEY_MAILJET_API_SECRET)

        val options = ClientOptions.builder()
            .apiKey(apiKey)
            .apiSecretKey(apiSecret)
            .build()

        MailjetClient(options)
    }

    private val message = buildString {
        append(PROJECT_NAME)
        append(" needs access to your Google Calendar to add emojis to your events.")

        appendLine()

        append("Automatic authorization failed or was denied. Please authorize the application manually")

        appendLine()

        append("This can be done by running the program on a local machine with GUI support following the OAuth 2.0 flow")

        appendLine()

        append("Afterwards copy the StoredCredential file in the tokens folder to the program on your machine hosting this program")

        appendLine()

        append("If you have any questions or need help, please contact the developer at contact@tomtruyen.dev")
    }

    fun send() = try {
        val to = properties.getProperty(KEY_MAILJET_TO_EMAIL)
        val from = properties.getProperty(KEY_MAILJET_FROM_EMAIL)

        val email = TransactionalEmail
            .builder()
            .to(SendContact(to))
            .from(SendContact(from, PROJECT_NAME))
            .subject("Authorization required for $PROJECT_NAME")
            .htmlPart(message)
            .trackOpens(TrackOpens.DISABLED)
            .build()

        val request = SendEmailsRequest
            .builder()
            .message(email)
            .build()

        request.sendWith(client)

        Logger.success("Mail sent successfully to $to")
    } catch (e: Exception) {
        Logger.error("Failed to send mail: ${e.message}")
    }
}
