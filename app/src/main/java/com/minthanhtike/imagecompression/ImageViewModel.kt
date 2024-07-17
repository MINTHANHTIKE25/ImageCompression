package com.minthanhtike.imagecompression

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ImageViewModel(private val workManagerRepo: WorkManagerRepo) : ViewModel() {
    var compressImg  = MutableStateFlow("")
    fun compressImage(imageUri: Uri){
        viewModelScope.launch {
            workManagerRepo.compressImg(imageUri)
            compressImg = workManagerRepo.compressImg
        }

    }
}