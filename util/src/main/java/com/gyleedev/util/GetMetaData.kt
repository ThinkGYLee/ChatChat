package com.gyleedev.util


import org.jsoup.Jsoup

fun getMetaData(comment: String): UriMetaData {
    try {
        val document = Jsoup.connect(detectUrl(comment)).get()
        val link = document.select("a").first()
        val absHref = link?.attr("abs:href")
        val doc = absHref?.let { Jsoup.connect(it).get() }
        val elements = doc?.select("meta[property^=og:]")
        val metaData = UriMetaData(url = comment)
        elements.let {
            it?.forEach { el ->
                when (el.attr("property")) {
                    "og:url" -> {
                        el.attr("content").let { content ->
                            metaData.url = content
                        }
                    }

                    "og:site_name" -> {
                        el.attr("content").let { content ->
                            metaData.name = content
                        }
                    }

                    "og:title" -> {
                        el.attr("content").let { content ->
                            metaData.title = content
                        }
                    }

                    "og:description" -> {
                        el.attr("content").let { content ->
                            metaData.description = content
                        }
                    }

                    "og:image" -> {
                        el.attr("content").let { content ->
                            metaData.imageUrl = content.toString()
                        }
                    }
                }
            }
        }
        return metaData
    } catch (e: Error) {
        return UriMetaData()
    }
}
