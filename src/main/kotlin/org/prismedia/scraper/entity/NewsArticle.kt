package org.prismedia.scraper.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "news_articles")
class NewsArticle(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val title: String,
    
    @Column(nullable = false)
    val author: String,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,
    
    @Column(nullable = false)
    val link: String,
    
    @Column(nullable = false)
    val source: String,
    
    @Column(nullable = true)
    val publishedDate: LocalDateTime? = null,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
