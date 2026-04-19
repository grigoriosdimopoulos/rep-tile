package com.reptile.gymtracker.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("MMM d · h:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDuration(): String {
    val totalMinutes = this / 60
    val seconds = this % 60
    return if (totalMinutes >= 60) {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        "${hours}h ${minutes}m"
    } else if (totalMinutes > 0) {
        "${totalMinutes}m ${seconds}s"
    } else {
        "${seconds}s"
    }
}

fun Float.toFormattedWeight(imperial: Boolean = false): String {
    return if (imperial) {
        "${(this * 2.20462f).roundToInt()} lbs"
    } else {
        "${this.roundToInt()} kg"
    }
}

fun Float.toFormattedHeight(imperial: Boolean = false): String {
    return if (imperial) {
        val totalInches = (this / 2.54f).roundToInt()
        val feet = totalInches / 12
        val inches = totalInches % 12
        "${feet}' ${inches}\""
    } else {
        "${this.roundToInt()} cm"
    }
}
