package com.example.wordwidget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.*
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.*
import androidx.glance.appwidget.lazy.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.color.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WordWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        Log.d("WordWidget", "Broadcast Received! Action: ${intent.action}")
        
        if (intent.action == "com.example.wordwidget.FORCE_SYNC") {
            Log.d("WordWidget", "Force Sync triggered, executing direct network request...")
            
            // 1. Tell Android to keep process alive for up to 10 seconds
            val pendingResult = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Immediately update UI to "Syncing..."
                    updateWidgetData(context, "Syncing...", "Connecting to Wordnik API...", "", "", "")

                    // Execute network request instantly, bypassing JobScheduler
                    val moshi = com.squareup.moshi.Moshi.Builder()
                        .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                        .build()

                    val retrofit = retrofit2.Retrofit.Builder()
                        .baseUrl("https://api.wordnik.com/v4/")
                        .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create(moshi))
                        .build()

                    val api = retrofit.create(WordnikService::class.java)
                    val apiKey = BuildConfig.WORDNIK_API_KEY

                    val wotd = api.getWordOfTheDay(apiKey)
                    val audioUrl = try {
                        val audioList = api.getAudio(wotd.word, apiKey)
                        audioList.firstOrNull()?.fileUrl ?: ""
                    } catch (e: Exception) { "" }

                    val definition = wotd.definitions.firstOrNull()

                    // Push successful data to UI
                    updateWidgetData(
                        context, 
                        wotd.word, 
                        definition?.text ?: "", 
                        definition?.partOfSpeech ?: "", 
                        wotd.note ?: "", 
                        audioUrl
                    )
                    Log.d("WordWidget", "Network request successful, UI updated.")

                } catch (e: Exception) {
                    e.printStackTrace()
                    updateWidgetData(context, "Network Error", e.localizedMessage ?: "Failed to connect", "", "", "")
                } finally {
                    // Tell OS we are done and it can release the 10-second lock
                    pendingResult.finish()
                }
            }
        }
    }

    // Helper function to cleanly push state to all widget instances
    private suspend fun updateWidgetData(
        context: Context, word: String, def: String, pos: String, etym: String, audio: String
    ) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(WordWidget::class.java).forEach { glanceId ->
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[WordKeys.WORD] = word
                    this[WordKeys.DEFINITION] = def
                    this[WordKeys.PART_OF_SPEECH] = pos
                    this[WordKeys.ETYMOLOGY] = etym
                    this[WordKeys.AUDIO_URL] = audio
                }
            }
        }
        WordWidget().updateAll(context)
    }
}

class WordWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val word = prefs[WordKeys.WORD] ?: "Tap sync to load..."
            val partOfSpeech = prefs[WordKeys.PART_OF_SPEECH] ?: ""
            val definition = prefs[WordKeys.DEFINITION] ?: ""
            val etymology = prefs[WordKeys.ETYMOLOGY] ?: ""
            val audioUrl = prefs[WordKeys.AUDIO_URL] ?: ""

            WidgetLayout(word, partOfSpeech, definition, etymology, audioUrl)
        }
    }
}

@Composable
fun WidgetLayout(word: String, pos: String, def: String, etym: String, audioUrl: String) {
    val context = LocalContext.current
    
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .padding(16.dp)
    ) {
        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            item {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word,
                        style = TextStyle(
                            color = ColorProvider(day = Color.White, night = Color.White),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )

                    if (audioUrl.isNotEmpty()) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_play),
                            contentDescription = "Play Pronunciation",
                            modifier = GlanceModifier.clickable(
                                actionSendBroadcast(
                                    Intent(context, AudioReceiver::class.java).apply {
                                        putExtra("AUDIO_URL", audioUrl)
                                    }
                                )
                            ).size(32.dp).padding(end = 8.dp)
                        )
                    }

                    val syncIntent = Intent(context, WordWidgetReceiver::class.java).apply {
                        action = "com.example.wordwidget.FORCE_SYNC"
                    }

                    Image(
                        provider = ImageProvider(R.drawable.ic_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.clickable(actionSendBroadcast(syncIntent))
                            .size(32.dp)
                    )
                }
            }
            item {
                Text(
                    text = pos,
                    style = TextStyle(
                        color = ColorProvider(day = Color.LightGray, night = Color.LightGray),
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = GlanceModifier.padding(top = 4.dp, bottom = 8.dp)
                )
            }
            item {
                Text(
                    text = def,
                    style = TextStyle(
                        color = ColorProvider(day = Color.White, night = Color.White), 
                        fontSize = 16.sp
                    ),
                    modifier = GlanceModifier.padding(bottom = 8.dp)
                )
            }
            item {
                Text(
                    text = etym,
                    style = TextStyle(
                        color = ColorProvider(day = Color.LightGray, night = Color.LightGray), 
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}