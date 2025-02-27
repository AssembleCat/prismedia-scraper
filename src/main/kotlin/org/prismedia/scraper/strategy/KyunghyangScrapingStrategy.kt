package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

@Component
class KyunghyangScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "경향신문"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                // UTM 파라미터 제거
                val cleanLink = entry.link?.split("?")?.firstOrNull() ?: entry.link

                // 첫 번째 기자 이름만 추출 (이메일 제외)
                val firstAuthor = entry.author?.split(",")?.firstOrNull()?.let { author ->
                    author.split(" 기자").first().trim()
                } ?: "경향신문"

                NewsArticle(
                    author = firstAuthor,
                    title = entry.title?.trim() ?: "",
                    link = cleanLink,
                    content = cleanContent(entry.description?.value),
                    source = "경향신문",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Kyunghyang article: ${entry.link}", e)
                null
            }
        }
    }
}
