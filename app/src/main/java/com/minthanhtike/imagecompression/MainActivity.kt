package com.minthanhtike.imagecompression

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minthanhtike.imagecompression.ui.theme.ImageCompressionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageCompressionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    var imageUri: Uri? by remember { mutableStateOf(null) }
    var imgBase64 by remember { mutableStateOf("") }

    val context = LocalContext.current
    val viewModel = DiContainer(context = context).imageViewModel
    val compressImg by viewModel.compressImg.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) {
        imageUri = it
    }
    Column(
        modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier.clickable {
                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )

        if (imageUri != null) {
            viewModel.compressImage(imageUri!!)
        }
        var names by remember { mutableStateOf("") }
        TextField(
            value = names,
            onValueChange = { names = it },
            modifier = modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
        )

        if (!compressImg.isNullOrEmpty()) {
            Image(
                bitmap = ImageUtils.base64toBitmap(compressImg).asImageBitmap(),
                contentDescription = "",
                modifier = modifier.wrapContentSize()
            )
        }

    }
}

fun Context.getImageUri(): Uri {
    val resources = this.resources

    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(R.drawable.default_img_ic))
        .appendPath(resources.getResourceTypeName(R.drawable.default_img_ic))
        .appendPath(resources.getResourceEntryName(R.drawable.default_img_ic))
        .build()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ImageCompressionTheme {
        Greeting("Android")
    }
}