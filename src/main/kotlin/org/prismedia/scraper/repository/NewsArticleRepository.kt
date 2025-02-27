package org.prismedia.scraper.repository

import org.prismedia.scraper.entity.NewsArticle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NewsArticleRepository : JpaRepository<NewsArticle, Long> {
    fun existsByLink(link: String): Boolean
}
