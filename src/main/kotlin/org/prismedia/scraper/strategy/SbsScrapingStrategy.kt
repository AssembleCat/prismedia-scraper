package org.prismedia.scraper.strategy

import com.rometools.rome.feed.synd.SyndFeed
import org.prismedia.scraper.entity.NewsArticle
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class SbsScrapingStrategy : BaseRssScrapingStrategy() {
    override fun supports(pressCode: String): Boolean = pressCode == "SBS"

    override fun scrape(feed: SyndFeed): List<NewsArticle> {
        return feed.entries.mapNotNull { entry ->
            try {
                // content:encoded에서 본문 추출 및 정제
                val content = entry.foreignMarkup
                    .firstOrNull { it.name == "encoded" && it.namespace?.prefix == "content" }
                    ?.value
                    ?.replace(Regex("<!-- tracking Pixel -->.*?<!-- //tracking Pixel -->"), "") // 트래킹 픽셀 제거
                    ?.replace(Regex("<p><a href=\"https://news\\.sbs\\.co\\.kr/news/appinstall.*?</p>"), "") // 앱 다운로드 링크 제거
                    ?.replace(Regex("<p>ⓒ SBS.*?</p>"), "") // 저작권 문구 제거
                    ?.replace(Regex("<iframe.*?</iframe>"), "") // iframe 제거

                // UTM 파라미터 제거
                val cleanLink = entry.link?.split("?")?.firstOrNull() ?: entry.link

                NewsArticle(
                    author = entry.author,
                    title = entry.title?.trim() ?: "",
                    link = cleanLink,
                    content = cleanContent(content ?: entry.description?.value),
                    source = "SBS",
                    publishedDate = entry.publishedDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
                )
            } catch (e: Exception) {
                logger.error("Error processing SBS article: ${entry.link}", e)
                null
            }
        }
    }
}
