package com.gyleedev.util

import android.text.SpannableString
import android.text.util.Linkify
import android.util.Patterns

fun detectUrl(comment: String): String {
    val spannableString = SpannableString.valueOf(comment)
    val matchFilter = Linkify.sUrlMatchFilter
    val pattern = Patterns.WEB_URL
    var url: String = ""
    val m = pattern.matcher(spannableString)
    while (m.find()) {
        val start = m.start()
        val end = m.end()
        if (matchFilter == null || matchFilter.acceptMatch(spannableString, start, end)) {
            url = spannableString.subSequence(start, end).toString()
        }
    }
    return url
}
