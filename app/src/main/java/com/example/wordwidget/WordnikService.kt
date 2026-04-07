package com.example.wordwidget

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class WotdResponse(
    val word: String,
    val definitions: List<Definition>,
    val note: String? // Often contains etymology (extraction later)
)

@JsonClass(generateAdapter = true)
data class Definition(
    val text: String,
    val partOfSpeech: String?
)

@JsonClass(generateAdapter = true)
data class AudioResponse(
    val fileUrl: String,
    val prnType: String?
)

interface WordnikService {
    @GET("words.json/wordOfTheDay")
    suspend fun getWordOfTheDay(@Query("api_key") apiKey: String): WotdResponse

    @GET("word.json/{word}/audio")
    suspend fun getAudio(
        @Path("word") word: String,
        @Query("api_key") apiKey: String
    ): List<AudioResponse>
}