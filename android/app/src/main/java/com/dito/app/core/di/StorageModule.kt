package com.dito.app.core.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 로컬 저장소 관련 의존성 모듈
 * - DataStore (토큰, 사용자 설정 등)
 * - Room Database (미션 데이터 등)
 *
 * TODO: 로컬 저장소 필요할 때 구현 예정
 */
@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    // TODO: DataStore 제공


    // TODO: Room Database 제공

}
