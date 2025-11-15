package com.dito.app.core.storage

import com.dito.app.core.data.home.HomeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeManager @Inject constructor(
    private val homeStorage: HomeStorage
) {
    private val _homeData = MutableStateFlow<HomeData?>(null)
    val homeData: StateFlow<HomeData?> = _homeData.asStateFlow()

    init {
        // 초기화 시 스토리지에서 데이터 로드
        _homeData.value = homeStorage.getHomeData()
    }

    fun saveHomeData(homeData: HomeData) {
        // 메모리 업데이트
        _homeData.value = homeData
        // 스토리지에 저장
        homeStorage.saveHomeData(homeData)
    }

    fun getCostumeUrl(): String? {
        return _homeData.value?.costumeUrl
    }
    
    fun clear() {
        _homeData.value = null
        homeStorage.clear()
    }
}
