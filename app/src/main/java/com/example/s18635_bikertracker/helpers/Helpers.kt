package com.example.s18635_bikertracker.helpers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Helpers {
    companion object{
        private val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        fun dateToString(date: LocalDateTime): String {
            return pattern.format(date)
        }

        fun stringToDate(date: String):LocalDateTime{
            return LocalDateTime.parse(date, pattern)
        }
    }
}