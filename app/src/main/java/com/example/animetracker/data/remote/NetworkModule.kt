package com.example.animetracker.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BANGUMI_BASE_URL = "https://api.bgm.tv/"
    private const val BILI_BASE_URL = "https://api.bilibili.com/"
    private const val DOUBAN_BASE_URL = "https://book.douban.com/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            chain.proceed(request)
        }
        .build()

    val bangumiApi: BangumiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BANGUMI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BangumiApi::class.java)
    }

    val biliApi: BiliApi by lazy {
        Retrofit.Builder()
            .baseUrl(BILI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BiliApi::class.java)
    }

    val doubanBookApi: DoubanBookApi by lazy {
        Retrofit.Builder()
            .baseUrl(DOUBAN_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DoubanBookApi::class.java)
    }
}
