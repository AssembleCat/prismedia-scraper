package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class JtbcScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "jtbc"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                val content = entry.description?.value ?: ""
                
                // JTBC 기사에서 이미지 태그와 캡션 제거
                val cleanContent = content.replace(Regex("<img[^>]+>"), "")
                    .replace(Regex("〈[^>]+〉"), "")
                    .trim()

                NewsArticle(
                    author = entry.author ?: "JTBC",
                    title = entry.title?.replace(Regex("\\s*\\[[^]]*\\]\\s*"), "")?.trim() ?: "",
                    link = entry.link,
                    content = cleanContent,
                    source = "JTBC",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing JTBC article: ${entry.link}", e)
                null
            }
        }
    }
}
