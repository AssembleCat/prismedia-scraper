package org.prismedia.scraper.service

import org.prismedia.scraper.entity.NewsArticle
import org.prismedia.scraper.repository.NewsArticleRepository
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.core.io.ClassPathResource
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.io.InputStreamReader
import java.io.BufferedReader

data class RssSource(
    val name: String,
    val category: String,
    val url: String
)

data class ScrapingResult(
    val totalArticles: Int,
    val newArticles: Int
) {
    val newArticleRatio: Double
        get() = if (totalArticles > 0) (newArticles.toDouble() / totalArticles) * 100 else 0.0
}

@Service
class RssScraperService(
    private val newsArticleRepository: NewsArticleRepository
) {
    private val logger = LoggerFactory.getLogger(RssScraperService::class.java)
    private val rssSources = loadRssSources()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    private fun loadRssSources(): List<RssSource> {
        val sources = mutableListOf<RssSource>()
        try {
            val resource = ClassPathResource("rss/rss.csv")
            BufferedReader(InputStreamReader(resource.inputStream)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 3 && parts[2].lowercase() != "none") {
                        sources.add(RssSource(parts[0], parts[1], parts[2]))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error loading RSS sources from CSV", e)
        }
        return sources
    }

    //@Scheduled(cron = "0 0 * * * *") // 매시간 정각에 실행
    @Scheduled(cron = "0 * * * * *") // 매분 0초에 실행
    fun scrapeRssFeeds() {
        val batchStartTime = LocalDateTime.now()
        logger.info("=== RSS Feed Scraping Started at ${batchStartTime.format(dateFormatter)} ===")
        logger.info("Target News Sources: ${rssSources.joinToString(", ") { it.name }}")
        
        val results = mutableMapOf<String, ScrapingResult>()
        var totalNewArticles = 0
        var totalArticles = 0
        
        rssSources.forEach { source ->
            try {
                logger.info("Processing ${source.name}...")
                val feed = fetchRssFeed(source.url)
                val result = processFeed(feed, source)
                results[source.name] = result
                totalNewArticles += result.newArticles
                totalArticles += result.totalArticles
            } catch (e: Exception) {
                logger.error("Error scraping RSS feed from ${source.url}", e)
                results[source.name] = ScrapingResult(0, 0)
            }
        }
        
        // 배치 실행 결과 요약 로깅
        val batchEndTime = LocalDateTime.now()
        logger.info("\n=== RSS Feed Scraping Summary ===")
        logger.info("Batch Execution Time: ${batchStartTime.format(dateFormatter)} - ${batchEndTime.format(dateFormatter)}")
        logger.info("Total Articles Processed: $totalArticles")
        logger.info("Total New Articles Found: $totalNewArticles")
        if (totalArticles > 0) {
            val totalRatio = (totalNewArticles.toDouble() / totalArticles) * 100
            logger.info("Overall New Article Ratio: %.2f%%".format(totalRatio))
        }
        
        // 각 언론사별 상세 결과 로깅
        logger.info("\n=== Results by News Source ===")
        results.forEach { (sourceName, result) ->
            logger.info("$sourceName:")
            logger.info("  - Total Articles: ${result.totalArticles}")
            logger.info("  - New Articles: ${result.newArticles}")
            logger.info("  - New Article Ratio: %.2f%%".format(result.newArticleRatio))
        }
        logger.info("=====================================\n")
    }

    private fun fetchRssFeed(rssUrl: String): SyndFeed {
        return SyndFeedInput().build(XmlReader(URL(rssUrl)))
    }

    private fun processFeed(feed: SyndFeed, source: RssSource): ScrapingResult {
        var totalArticles = 0
        var newArticles = 0
        
        feed.entries.forEach { entry ->
            totalArticles++
            val link = entry.link
            if (!newsArticleRepository.existsByLink(link)) {
                newArticles++
                val article = NewsArticle(
                    title = entry.title ?: "",
                    author = entry.author ?: "Unknown",
                    content = entry.description?.value ?: "",
                    link = link,
                    source = source.name,
                    publishedDate = entry.publishedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                )
                newsArticleRepository.save(article)
            }
        }
        
        return ScrapingResult(totalArticles, newArticles)
    }
}
