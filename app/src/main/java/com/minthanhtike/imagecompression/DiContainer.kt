package com.minthanhtike.imagecompression

import android.content.Context

class DiContainer(context: Context) {

    val imageViewModel = ImageViewModel(WorkManagerRepo(context))
}