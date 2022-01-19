package com.es.careapp.data.work

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson

class WeatherForecast {

    fun interface WeatherCallback {
        fun onWeather(data: Weather?)
    }

    companion object {

        fun serializeWeatherToJson(weather: Weather): String {
            val gson = Gson()
            return gson.toJson(weather)
        }

        fun deserializeWeatherFromJson(jsonString: String): Weather {
            val gson = Gson()
            return gson.fromJson(jsonString, Weather::class.java)
        }
    }

    fun fetchWeather(
        context: Context,
        viewLifecycleOwner: LifecycleOwner,
        callback: WeatherCallback
    ) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request =
            OneTimeWorkRequestBuilder<WeatherWorker>().setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueue(request)
        WorkManager.getInstance(context).getWorkInfoByIdLiveData(request.id)
            .observe(viewLifecycleOwner, {
                if (it != null && it.state.isFinished) {
                    val weatherString = it.outputData.getString(WeatherWorker.DATA_TAG)
                    if (weatherString == WeatherWorker.WEATHER_ERROR) {
                        callback.onWeather(null)
                    } else {
                        weatherString?.let {
                            val weather = deserializeWeatherFromJson(weatherString)
                            callback.onWeather(weather)
                        }

                    }
                }
            })
    }
}
