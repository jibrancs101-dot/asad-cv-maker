package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.cvDataStore: DataStore<Preferences> by preferencesDataStore(name = "asad_cv_maker_prefs")

class CvRepository(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(PersistedCvState::class.java)

    private object Keys {
        val CV_STATE_JSON = stringPreferencesKey("cv_state_json")
    }

    val stateFlow: Flow<PersistedCvState> = context.cvDataStore.data.map { prefs ->
        val json = prefs[Keys.CV_STATE_JSON]
        if (json.isNullOrBlank()) {
            PersistedCvState(
                profile = SampleCvData.defaultProfile(),
                settings = CvSettings()
            )
        } else {
            runCatching { adapter.fromJson(json) }.getOrNull()
                ?: PersistedCvState(
                    profile = SampleCvData.defaultProfile(),
                    settings = CvSettings()
                )
        }
    }

    suspend fun saveState(state: PersistedCvState) {
        val json = adapter.toJson(state)
        context.cvDataStore.edit { prefs ->
            prefs[Keys.CV_STATE_JSON] = json
        }
    }
}
