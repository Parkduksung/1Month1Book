package com.example.englishvocabulary.di

import com.example.englishvocabulary.network.api.KakaoApi
import com.example.englishvocabulary.network.api.NaverApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private const val KAKAO_URL = "https://dapi.kakao.com/"
    private const val NAVER_URL = "https://openapi.naver.com/"

    @Singleton
    @Provides
    fun provideKakaoApi(): KakaoApi {
        return Retrofit.Builder()
            .baseUrl(KAKAO_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoApi::class.java)
    }

    @Singleton
    @Provides
    fun provideNaverApi(): NaverApi {
        return Retrofit.Builder()
            .baseUrl(NAVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverApi::class.java)
    }

}