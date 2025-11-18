package com.dito.app.core.repository

import com.dito.app.core.data.shop.PurchaseRequest
import com.dito.app.core.data.shop.PurchaseResponse
import com.dito.app.core.data.shop.ShopResponse
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepository @Inject constructor(
    private val apiService: ApiService,
    private val authTokenManager: AuthTokenManager
) {
    suspend fun getShopItems(type: String, page: Int): Result<ShopResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
                ?: return@withContext Result.failure(Exception("로그인이 필요합니다"))

            val response = apiService.getShopItems(
                token = "Bearer $accessToken",
                type = type,
                pageNumber = page
            )

            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.error == false) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.body()!!.message ?: "상점 아이템 로드 실패"))
                }
            } else {
                val errorMessage = "상점 아이템 로드 실패 (code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purchaseItem(itemId: Long): Result<PurchaseResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            val request = PurchaseRequest(itemId = itemId)
            val response = apiService.purchaseItem(
                token = "Bearer $accessToken",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.error == false) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.body()!!.message ?: "아이템 구매 실패"))
                }
            } else {
                val errorMessage = "아이템 구매 실패 (code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun equipItem(itemId: Long): Result<PurchaseResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = authTokenManager.getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("로그인이 필요합니다"))
            }

            val request = PurchaseRequest(itemId = itemId)
            val response = apiService.equipItem(
                token = "Bearer $accessToken",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                if (response.body()!!.error == false) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception(response.body()!!.message ?: "아이템 적용 실패"))
                }
            } else {
                val errorMessage = "아이템 적용 실패 (code: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
