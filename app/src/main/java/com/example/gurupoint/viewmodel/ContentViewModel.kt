package com.example.gurupoint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gurupoint.data.model.*
import com.example.gurupoint.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContentViewModel(private val contentRepository: ContentRepository) : ViewModel() {

    private val _appConfig = MutableStateFlow(AppSystemConfig())
    val appConfig: StateFlow<AppSystemConfig> = _appConfig.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _ads = MutableStateFlow<List<AdItem>>(emptyList())
    val ads: StateFlow<List<AdItem>> = _ads.asStateFlow()

    init {
        loadContent()
    }

    fun loadContent() {
        viewModelScope.launch {
            _appConfig.value = contentRepository.getAppConfig()
            _banners.value = contentRepository.getBanners()
            _announcements.value = contentRepository.getAnnouncements()
            _articles.value = contentRepository.getArticles()
            _videos.value = contentRepository.getVideos()
            _ads.value = contentRepository.getAds(AdPlacement.HOMEPAGE_BANNER)
        }
    }

    fun toggleMaintenanceMode(enabled: Boolean) {
        viewModelScope.launch {
            val updated = _appConfig.value.copy(isMaintenanceMode = enabled)
            contentRepository.updateAppConfig(updated)
            _appConfig.value = updated
        }
    }

    fun toggleEmergencyStopGames(stopped: Boolean) {
        viewModelScope.launch {
            val updated = _appConfig.value.copy(isEmergencyGamesStopped = stopped)
            contentRepository.updateAppConfig(updated)
            _appConfig.value = updated
        }
    }

    fun saveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            contentRepository.saveAnnouncement(announcement)
            loadContent()
        }
    }

    fun saveArticle(article: Article) {
        viewModelScope.launch {
            contentRepository.saveArticle(article)
            loadContent()
        }
    }

    fun saveVideo(video: VideoItem) {
        viewModelScope.launch {
            contentRepository.saveVideo(video)
            loadContent()
        }
    }

    fun saveAd(ad: AdItem) {
        viewModelScope.launch {
            contentRepository.saveAd(ad)
            loadContent()
        }
    }
}
