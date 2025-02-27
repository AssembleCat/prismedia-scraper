package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class DongaScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "동아일보"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                NewsArticle(
                    author = entry.author ?: "동아일보",
                    title = entry.title?.trim() ?: "",
                    link = entry.link,
                    content = cleanContent(entry.description?.value),
                    source = "동아일보",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Donga article: ${entry.link}", e)
                null
            }
        }
    }
}
