package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class MaekyungScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "매일경제"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                NewsArticle(
                    author = entry.author ?: "매일경제",
                    title = entry.title?.trim() ?: "",
                    link = entry.link,
                    content = cleanContent(entry.description?.value),
                    source = "매일경제",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Maekyung article: ${entry.link}", e)
                null
            }
        }
    }
}
