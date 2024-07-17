package com.minthanhtike.imagecompression

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect

const val KEY_IMAGE_URI = "IMAGE KEY"

class WorkManagerRepo(context: Context) {
    val compressImg = MutableStateFlow("")
    private val workManager = WorkManager.getInstance(context)

    suspend fun compressImg(imgUri: Uri) {
        val compressBuilder = OneTimeWorkRequestBuilder<CompressWorker>()
            .setInputData(createInputDataForWorkRequest(imgUri))
            .build()

        workManager.enqueue(compressBuilder)
        workManager.getWorkInfoByIdFlow(compressBuilder.id).collect { workInfo ->
            when (workInfo.state) {
                WorkInfo.State.SUCCEEDED -> {
                    compressImg.value = workInfo.outputData.getString(KEY_IMAGE_URI) ?: ""
                }

                WorkInfo.State.FAILED -> {

                }

                else -> {}
            }
        }
    }


    /**
     * Creates the input data bundle which includes the blur level to
     * update the amount of blur to be applied and the Uri to operate on
     * @return Data which contains the Image Uri as a String and blur level as an Integer
     */
    private fun createInputDataForWorkRequest(imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString())
        return builder.build()
    }
}