package com.tomtruyen.emojiprefixer.utils

object Logger {
    fun warn(message: String) {
        println("\u001B[33m[WARN] $message\u001B[0m")
    }

    fun error(message: String) {
        println("\u001B[31m[ERROR] $message\u001B[0m")
    }

    fun info(message: String) {
        println("\u001B[34m[INFO] $message\u001B[0m")
    }

    fun success(message: String) {
        println("\u001B[32m[SUCCESS] $message\u001B[0m")
    }
}