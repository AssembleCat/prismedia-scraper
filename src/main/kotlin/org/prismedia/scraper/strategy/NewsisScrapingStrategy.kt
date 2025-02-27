package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class NewsisScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "뉴시스"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                // description에서 이메일 주소 제거
                val content = entry.description?.value?.replace(Regex("◎공감언론 뉴시스.*$"), "")

                // 작성자에서 "기자" 문구 제거
                val author = entry.foreignMarkup
                    .firstOrNull { it.name == "creator" && it.namespace?.prefix == "dc" }
                    ?.value?.replace(" 기자", "")
                    ?: "뉴시스"

                NewsArticle(
                    author = author,
                    title = entry.title?.trim() ?: "",
                    link = entry.link,
                    content = cleanContent(content),
                    source = "뉴시스",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing Newsis article: ${entry.link}", e)
                null
            }
        }
    }
}
