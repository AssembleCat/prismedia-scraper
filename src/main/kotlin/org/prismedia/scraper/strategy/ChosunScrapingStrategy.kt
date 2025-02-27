package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class ChosunScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "조선일보"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                NewsArticle(
                    author = entry.author ?: "조선일보",
                    title = entry.title,
                    link = entry.link,
                    content = extractMainContent(entry.description?.value, entry.contents.firstOrNull()?.value),
                    source = "조선일보",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Chosun article: ${entry.link}", e)
                null
            }
        }
    }
}
