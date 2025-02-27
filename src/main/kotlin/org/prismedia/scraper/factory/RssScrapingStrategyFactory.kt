package org.prismedia.scraper.factory

import org.prismedia.scraper.strategy.DefaultScrapingStrategy
import org.prismedia.scraper.strategy.RssScrapingStrategy
import org.springframework.stereotype.Component

@Component
class RssScrapingStrategyFactory(
    private val strategies: List<RssScrapingStrategy>,
    private val defaultStrategy: DefaultScrapingStrategy
) {
    fun getStrategy(pressCode: String): RssScrapingStrategy {
        return strategies.find { it.supports(pressCode) } ?: defaultStrategy
    }
}
