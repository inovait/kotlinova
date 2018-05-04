package si.inova.kotlinova.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import java.io.File
import java.io.IOException

/**
 * @author Jan Grah
 */

@Throws(IOException::class)
fun createTemporaryImage(context: Context): File {
    val imageFileName = "profile_photo_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
}

@Throws(IOException::class)
fun deleteTemporaryImage(profilePhotoPath: Uri) {
    profilePhotoPath.toFile().delete()
}