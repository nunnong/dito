package com.dito.app.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 네트워크 관련 의존성 모듈
 * - Retrofit
 * - OkHttpClient
 * - API 서비스 인터페이스
 *
 * TODO: 서버 API 준비되면 구현 예정
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // TODO: Retrofit 인스턴스 제공
    // @Provides
    // @Singleton
    // fun provideRetrofit(): Retrofit { ... }

    // TODO: OkHttpClient 제공
    // @Provides
    // @Singleton
    // fun provideOkHttpClient(): OkHttpClient { ... }

    // TODO: API 서비스 제공
    // @Provides
    // @Singleton
    // fun provideApiService(retrofit: Retrofit): ApiService { ... }
}
