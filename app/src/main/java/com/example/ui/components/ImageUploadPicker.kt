package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.BuyJumpBlue
import com.example.ui.theme.BuyJumpOrange
import kotlinx.coroutines.launch

@Composable
fun ImageUploadPicker(
    images: List<String>,
    onImagesChanged: (List<String>) -> Unit,
    onUploadUri: suspend (Uri) -> Result<String>,
    modifier: Modifier = Modifier,
    maxImages: Int = 4,
    label: String = "Product Images (Device / Local Disk)"
) {
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Modern zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadError = null
            uploadSuccessMessage = null
            coroutineScope.launch {
                val result = onUploadUri(uri)
                isUploading = false
                result.onSuccess { savedUri ->
                    onImagesChanged(images + savedUri)
                    uploadSuccessMessage = "Image uploaded successfully from local device!"
                }.onFailure { err ->
                    uploadError = "Failed to upload image: ${err.message}"
                }
            }
        }
    }

    // Fallback file picker for documents / disk
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadError = null
            uploadSuccessMessage = null
            coroutineScope.launch {
                val result = onUploadUri(uri)
                isUploading = false
                result.onSuccess { savedUri ->
                    onImagesChanged(images + savedUri)
                    uploadSuccessMessage = "Image uploaded from storage!"
                }.onFailure { err ->
                    uploadError = "Upload error: ${err.message}"
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${images.size} / $maxImages",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Images List with Remove & Replace
        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                items(images) { imageUri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Uploaded Image Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Remove Image Button
                        IconButton(
                            onClick = { onImagesChanged(images - imageUri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Upload Trigger Box
        if (images.size < maxImages) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(
                        width = 1.5.dp,
                        color = if (uploadError != null) MaterialTheme.colorScheme.error else BuyJumpBlue.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable(enabled = !isUploading) {
                        try {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } catch (e: Exception) {
                            // Fallback to GetContent
                            documentPickerLauncher.launch("image/*")
                        }
                    }
                    .testTag("upload_image_picker_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BuyJumpOrange,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Uploading image from local device...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BuyJumpBlue
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Icon",
                            tint = BuyJumpBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Choose from Device Storage / Gallery",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BuyJumpBlue
                        )
                        Text(
                            text = "JPG, PNG, WEBP supported (Max 10MB)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Error message
        if (uploadError != null) {
            Text(
                text = uploadError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Success message
        if (uploadSuccessMessage != null) {
            Text(
                text = uploadSuccessMessage.orEmpty(),
                color = Color(0xFF10B981),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
