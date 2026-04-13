package com.example.wordwidget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class DailyWordWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.wordnik.com/v4/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val api = retrofit.create(WordnikService::class.java)
            
            val apiKey = BuildConfig.WORDNIK_API_KEY
            val wotd = api.getWordOfTheDay(apiKey)
            val audioList = api.getAudio(wotd.word, apiKey)
            
            val audioUrl = audioList.firstOrNull()?.fileUrl ?: ""
            val definition = wotd.definitions.firstOrNull()
            
            // Fetch active widget instances
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(WordWidget::class.java)

            // Loop and update state for each
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WordKeys.WORD] = wotd.word
                        this[WordKeys.PART_OF_SPEECH] = definition?.partOfSpeech ?: ""
                        this[WordKeys.DEFINITION] = definition?.text ?: ""
                        this[WordKeys.ETYMOLOGY] = wotd.note ?: ""
                        this[WordKeys.AUDIO_URL] = audioUrl
                    }
                }
            }

            WordWidget().updateAll(context)
            Result.success()
                } catch (e: Exception) {
                    e.printStackTrace()
                    
                    // Force error message onto widget screen
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(WordWidget::class.java)
                    
                    glanceIds.forEach { glanceId ->
                        updateAppWidgetState(context, glanceId) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[WordKeys.WORD] = "Network Error"
                                this[WordKeys.PART_OF_SPEECH] = ""
                                this[WordKeys.DEFINITION] = e.localizedMessage ?: e.toString()
                                this[WordKeys.ETYMOLOGY] = "Check your API key or internet connection."
                            }
                        }
                    }
                    WordWidget().updateAll(context)
                    Result.failure()
                }
    }
}

object WordKeys {
    val WORD = stringPreferencesKey("word")
    val PART_OF_SPEECH = stringPreferencesKey("part_of_speech")
    val DEFINITION = stringPreferencesKey("definition")
    val ETYMOLOGY = stringPreferencesKey("etymology")
    val AUDIO_URL = stringPreferencesKey("audio_url")
}