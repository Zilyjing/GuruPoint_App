package com.example.gurupoint.data.repository

import com.example.gurupoint.data.local.SessionManager
import com.example.gurupoint.data.model.*
import kotlinx.coroutines.delay

class ContentRepository(private val sessionManager: SessionManager) {

    private var appConfig = AppSystemConfig()

    private val mockBanners = mutableListOf(
        BannerItem("b1", "Welcome Offer", "https://via.placeholder.com/600x200/FFB300/000000?text=Welcome+Bonus+500+Coins", isActive = true, displayOrder = 1),
        BannerItem("b2", "Refer & Earn", "https://via.placeholder.com/600x200/3B82F6/FFFFFF?text=Earn+100+Coins+Per+Referral", isActive = true, displayOrder = 2)
    )

    private val mockAnnouncements = mutableListOf(
        Announcement("a1", "System Announcement", "Live result updates for Delhi Bazar & Shree Ganesh are active! Enjoy free virtual coins.", isActive = true, startDate = "2025-02-18", endDate = "2025-02-28")
    )

    private val mockArticles = mutableListOf(
        Article("art_1", "How to Enjoy Mode A (00-99) Games", "Guides", null, "Mode A allows selecting any number between 00 and 99. If your selected number matches the declared result, you win 90x Free Virtual Coins!", "GuruPoint Team", "2025-02-18", isActive = true),
        Article("art_2", "Virtual Coins Ledger System Explained", "Platform", null, "GuruPoint keeps an immutable ledger history of every coin addition, deduction, reward, and bonus so you have complete visibility over your wallet balance.", "GuruPoint Team", "2025-02-18", isActive = true)
    )

    private val mockVideos = mutableListOf(
        VideoItem("vid_1", "GuruPoint App Platform Introduction", "Tutorials", null, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "Learn how to register, claim bonus coins, and select numbers in GuruPoint app.", isActive = true)
    )

    private val mockAds = mutableListOf(
        AdItem("ad_1", "Promotional Offer Ad", null, "Get 100 Bonus Coins on inviting friends using your Referral Code!", null, AdPlacement.HOMEPAGE_BANNER, isActive = true)
    )

    suspend fun getAppConfig(): AppSystemConfig {
        delay(200)
        return appConfig
    }

    suspend fun updateAppConfig(config: AppSystemConfig): Result<Unit> {
        delay(300)
        appConfig = config
        return Result.success(Unit)
    }

    suspend fun getBanners(): List<BannerItem> {
        delay(200)
        return mockBanners.filter { it.isActive }
    }

    suspend fun getAnnouncements(): List<Announcement> {
        delay(200)
        return mockAnnouncements.filter { it.isActive }
    }

    suspend fun getArticles(): List<Article> {
        delay(200)
        return mockArticles.filter { it.isActive }
    }

    suspend fun getVideos(): List<VideoItem> {
        delay(200)
        return mockVideos.filter { it.isActive }
    }

    suspend fun getAds(placement: AdPlacement): List<AdItem> {
        delay(200)
        return mockAds.filter { it.isActive && it.placement == placement }
    }

    // Admin Content Management
    suspend fun saveAnnouncement(announcement: Announcement): Result<Unit> {
        delay(300)
        val idx = mockAnnouncements.indexOfFirst { it.id == announcement.id }
        if (idx != -1) mockAnnouncements[idx] = announcement else mockAnnouncements.add(0, announcement)
        return Result.success(Unit)
    }

    suspend fun saveArticle(article: Article): Result<Unit> {
        delay(300)
        val idx = mockArticles.indexOfFirst { it.id == article.id }
        if (idx != -1) mockArticles[idx] = article else mockArticles.add(0, article)
        return Result.success(Unit)
    }

    suspend fun saveVideo(video: VideoItem): Result<Unit> {
        delay(300)
        val idx = mockVideos.indexOfFirst { it.id == video.id }
        if (idx != -1) mockVideos[idx] = video else mockVideos.add(0, video)
        return Result.success(Unit)
    }

    suspend fun saveAd(ad: AdItem): Result<Unit> {
        delay(300)
        val idx = mockAds.indexOfFirst { it.id == ad.id }
        if (idx != -1) mockAds[idx] = ad else mockAds.add(0, ad)
        return Result.success(Unit)
    }
}
