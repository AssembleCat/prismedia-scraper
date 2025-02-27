package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class YonhapScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "연합뉴스"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                // content:encoded 태그에서 전체 본문 가져오기
                val fullContent = entry.foreignMarkup
                    .firstOrNull { it.name == "encoded" && it.namespace?.prefix == "content" }
                    ?.value

                NewsArticle(
                    author = entry.foreignMarkup
                        .firstOrNull { it.name == "creator" && it.namespace?.prefix == "dc" }
                        ?.value ?: "연합뉴스",
                    title = entry.title?.trim() ?: "",
                    link = entry.link,
                    content = cleanContent(fullContent ?: entry.description?.value),
                    source = "연합뉴스",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Yonhap article: ${entry.link}", e)
                null
            }
        }
    }
}
