package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class DefaultScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = true // 기본 전략이므로 항상 true 반환

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                NewsArticle(
                    author = entry.author ?: "Unknown",
                    title = entry.title,
                    link = entry.link,
                    content = extractMainContent(entry.description?.value, entry.contents.firstOrNull()?.value),
                    source = feed.title ?: "Unknown",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing article from ${feed.title}: ${entry.link}", e)
                null
            }
        }
    }
}
