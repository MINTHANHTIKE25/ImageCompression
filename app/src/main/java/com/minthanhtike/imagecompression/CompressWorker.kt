package com.minthanhtike.imagecompression

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class

CompressWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val context = applicationContext
                val uri = inputData.getString(KEY_IMAGE_URI)
                val imgBase64 =
                    ImageUtils.encodeToBase64(imgUri = Uri.parse(uri), context = context)
                val output = workDataOf(KEY_IMAGE_URI to imgBase64)
                Log.d("SUCCESS", "doWork: ${output.getString(KEY_IMAGE_URI)}")
                Result.success(output)
            } catch (e: Exception) {
                Log.d("Error", "doWork: ${e.localizedMessage}")
                Result.failure()
            }
        }
    }
}