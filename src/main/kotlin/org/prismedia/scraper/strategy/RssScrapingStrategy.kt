package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.slf4j.LoggerFactory

interface RssScrapingStrategy {
    fun scrape(feed: SyndFeed): List<NewsArticle>
    fun supports(pressCode: String): Boolean
}

abstract class BaseRssScrapingStrategy : RssScrapingStrategy {
    protected val logger = LoggerFactory.getLogger(this::class.java)
    
    protected fun cleanContent(content: String?): String {
        return content?.replace(Regex("<[^>]*>"), "")?.trim() ?: ""
    }
    
    protected fun extractMainContent(description: String?, content: String?): String {
        return when {
            !content.isNullOrBlank() -> cleanContent(content)
            !description.isNullOrBlank() -> cleanContent(description)
            else -> ""
        }
    }
}
