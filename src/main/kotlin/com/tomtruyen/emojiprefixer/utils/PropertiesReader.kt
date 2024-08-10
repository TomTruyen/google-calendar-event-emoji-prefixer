package com.tomtruyen.emojiprefixer.utils

import java.util.*

object PropertiesReader {
    private const val PATH_ENVIRONMENT = "/local.properties"

    fun loadProperties() = Properties().apply {
        load(CalendarAuthorizationMailer::class.java.getResourceAsStream(PATH_ENVIRONMENT))
    }
}