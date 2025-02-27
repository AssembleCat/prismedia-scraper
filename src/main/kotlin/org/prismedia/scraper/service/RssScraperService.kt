package org.prismedia.scraper.service

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.prismedia.scraper.entity.NewsArticle
import org.prismedia.scraper.factory.RssScrapingStrategyFactory
import org.prismedia.scraper.repository.NewsArticleRepository
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class RssScraperService(
    private val newsArticleRepository: NewsArticleRepository,
    private val strategyFactory: RssScrapingStrategyFactory
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun loadRssSources(): List<Triple<String, String, String>> {
        val sources = mutableListOf<Triple<String, String, String>>()
        val resource = ClassPathResource("rss/rss.csv")
        BufferedReader(InputStreamReader(resource.inputStream)).use { reader ->
            reader.lineSequence().forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 3) {
                    sources.add(Triple(parts[0], parts[1], parts[2]))
                }
            }
        }
        return sources
    }

    @Scheduled(cron = "0 0 * * * *") // 매시간 정각에 실행
    fun scrapeRssFeeds() {
        val batchStartTime = LocalDateTime.now()
        logger.info("=== RSS Feed Scraping Started at ${batchStartTime.format(dateFormatter)} ===")

        var totalArticles = 0
        var newArticles = 0

        loadRssSources().forEach { (pressCode, _, url) ->
            try {
                val feed = fetchFeed(url)
                if (feed != null) {
                    val strategy = strategyFactory.getStrategy(pressCode)
                    val articles = strategy.scrape(feed)
                    
                    totalArticles += articles.size
                    newArticles += saveNewArticles(articles)
                    
                    logger.info("Processed ${articles.size} articles from $pressCode")
                }
            } catch (e: Exception) {
                logger.error("Error processing RSS feed for $pressCode: $url", e)
            }
        }

        val batchEndTime = LocalDateTime.now()
        logger.info("=== RSS Feed Scraping Completed at ${batchEndTime.format(dateFormatter)} ===")
        logger.info("Total articles processed: $totalArticles")
        logger.info("New articles saved: $newArticles")
    }

    private fun fetchFeed(url: String): SyndFeed? {
        return try {
            val uri = URI(url)
            uri.toURL().openStream().use { stream ->
                val input = SyndFeedInput()
                input.build(XmlReader(stream))
            }
        } catch (e: Exception) {
            logger.error("Error fetching feed from $url", e)
            null
        }
    }

    private fun saveNewArticles(articles: List<NewsArticle>): Int {
        var count = 0
        articles.forEach { article ->
            try {
                if (!newsArticleRepository.existsByLinkAndSource(article.link, article.source)) {
                    newsArticleRepository.save(article)
                    count++
                }
            } catch (e: Exception) {
                logger.error("Error saving article: ${article.link}", e)
            }
        }
        return count
    }
}
