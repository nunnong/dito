package com.dito.app.core.di

import android.content.Context
import com.dito.app.core.network.AIService
import com.dito.app.core.network.ApiService
import com.dito.app.core.storage.AuthTokenManager
import com.google.android.datatransport.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 네트워크 관련 의존성 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    private const val BASE_URL = "http://52.78.96.102:8080/"

    //실제 기기 테스트 시 -> PC_IP:8123 (PC_IP는 같은 Wi-Fi 네트워크에서 PC의 IP)
    private const val AI_BASE_URL = "http://52.78.96.102:8080/"

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
            isLenient = true
            explicitNulls = false
        }
    }

    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            // 디버그: 본문까지, 릴리스: 로그 끔
            level = HttpLoggingInterceptor.Level.BODY

            // 민감 헤더 마스킹
            redactHeader("Authorization")
            // 필요 시 추가 마스킹 예:
            // redactHeader("Cookie")
            // redactHeader("Set-Cookie")
        }
    }


    /**
     * 일반 API용 OkHttpClient (인증 포함)
     */
    @Provides
    @Singleton
    @ApiOkHttpClient
    fun provideApiOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val authTokenManager = AuthTokenManager(context)

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(createAuthInterceptor(authTokenManager))
            .addInterceptor(createLoggingInterceptor())
            .build()
    }


    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        @ApiOkHttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }


    @Provides
    @Singleton
    fun provideApiService(@ApiRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }



    /**
     * AI 서버용 OkHttpClient (인증 없음)
     */
    @Provides
    @Singleton
    @AiOkHttpClient
    fun provideAiOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val authTokenManager = AuthTokenManager(context)

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(createAuthInterceptor(authTokenManager))
            .addInterceptor(createLoggingInterceptor())
            .build()
    }


    @Provides
    @Singleton
    @AiRetrofit
    fun provideAiRetrofit(
        @AiOkHttpClient okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }


    @Provides
    @Singleton
    fun provideAIService(@AiRetrofit retrofit: Retrofit): AIService {
        return retrofit.create(AIService::class.java)
    }



    /**
     * 인증 Interceptor 생성
     */
    private fun createAuthInterceptor(authTokenManager: AuthTokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = authTokenManager.getBearerToken()

            val newRequest = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", token)
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }
    }
}

// ========== Qualifiers (구분용 어노테이션) ==========

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AiOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AiRetrofit