package com.example.wordwidget

import android.content.Context
import android.content.Intent
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
import androidx.glance.color.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WordWidget()
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
                            // EXPLICITLY DEFINING DAY AND NIGHT COLOURS HERE
                            color = ColorProvider(day = Color.White, night = Color.White),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )

                    if (audioUrl.isNotEmpty()) {
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_media_play),
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

                    Image(
                        provider = ImageProvider(android.R.drawable.ic_popup_sync),
                        contentDescription = "Refresh",
                        modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>())
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

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val request = OneTimeWorkRequestBuilder<DailyWordWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }
}