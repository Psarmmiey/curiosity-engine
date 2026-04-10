package com.curiosityengine.app.feature.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.curiosityengine.app.util.Constants
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    fun shareImage(context: Context, bitmap: Bitmap, title: String) {
        // 1. Save bitmap to cacheDir/share/share_card.png
        val shareDir = File(context.cacheDir, "share").also { it.mkdirs() }
        val imageFile = File(shareDir, "share_card.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // 2. Get FileProvider URI
        val uri = FileProvider.getUriForFile(
            context,
            Constants.FILE_PROVIDER_AUTHORITY,
            imageFile
        )

        // 3. Build ACTION_SEND intent with type "image/png"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, title)
            // 4. Grant URI read permission
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 5. Start chooser
        val chooser = Intent.createChooser(sendIntent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
