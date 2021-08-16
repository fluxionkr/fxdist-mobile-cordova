package kr.fluxion.cordova.plugin.camera

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import kr.fluxion.fx.BuildConfig
import kr.fluxion.fx.utils.getBase64
import org.apache.cordova.SimpleCordovaPlugin
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Camera : SimpleCordovaPlugin() {

    lateinit var currentPhotoUri: Uri

    override fun execute(args: JSONArray): Boolean = when (action) {
        ACTION_OPEN_AOSP_CAMERA -> dispatchTakePictureIntent().run { true }
        else -> false
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? =
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
            File.createTempFile(getPhotoFilePrefix(), ".jpg", it).also {
                currentPhotoUri = Uri.fromFile(it)
            }
        }

    private fun getPhotoFilePrefix(): String =
        "fxdist_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_"

    private fun dispatchTakePictureIntent() {
        if (hasCameraPermissions()) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
                intent.resolveActivity(context.packageManager)?.also {
                    try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }?.also {
                        Logger.i("PhotoFileUri: $currentPhotoUri")
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(context, it))
                        cordova.startActivityForResult(this, intent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        } else {
            requestCameraPerm()
        }
    }

    private fun getFileProviderAuthority(context: Context) =
        "${context.packageName}.fileprovider"

    private fun getUriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, getFileProviderAuthority(context), file)

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                showPickedPhotoInfo(context, currentPhotoUri)
                currentPhotoUri.path?.let {
                    val imageData = decodeSampledBitmapFromUri(it).getBase64()
                    Formatter.formatFileSize(context, imageData.length.toLong()).run {
                        Logger.d("ScaledImageSize: $this")
                    }
                    sendSuccessCallback("success", getImageData(imageData))
                } ?: sendErrorCallback(4201, "Image content uri is null.")
            } else {
                sendErrorCallback(4202, "Image capture has been canceled.")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent)
        }
    }

    private fun decodeSampledBitmapFromUri(uriPath: String): Bitmap = BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(uriPath, this)
        val scaledWidth: Int = outWidth / 8
        val scaledHeight: Int = outHeight / 8
        Logger.d("Scaled '$outWidth : $outHeight' to '$scaledWidth : $scaledHeight'")
        inSampleSize = calculateInSampleSize(this, scaledWidth, scaledHeight)
        inJustDecodeBounds = false
        val scaledBitmap: Bitmap = BitmapFactory.decodeFile(uriPath, this)
        val matrix: Matrix = getMatrix(uriPath)
        Bitmap.createBitmap(scaledBitmap, 0, 0, scaledWidth, scaledHeight, matrix, true)
    }

    @Throws(IOException::class)
    private fun getMatrix(fileName: String): Matrix = Matrix().apply {
        when (ExifInterface(fileName).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
            6 -> postRotate(90f)
            3 -> postRotate(180f)
            8 -> postRotate(270f)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getImageData(imageData: String): String = Gson().toJson(JsonObject().apply {
        addProperty("data", imageData)
    })

    private fun hasCameraPermissions(): Boolean = cordova.hasPermission(CAMERA_PERMISSION)

    private fun requestCameraPerm(): Unit =
        cordova.requestPermission(this, REQ_CODE_PERM_CAMERA, CAMERA_PERMISSION)

    override fun onRequestPermissionResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQ_CODE_PERM_CAMERA ->
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    dispatchTakePictureIntent()
                } else {
                    Logger.w("Camera permission is not granted.")
                    sendErrorCallback(4200, "Camera permission is not granted.")
                }
            else -> super.onRequestPermissionResult(requestCode, permissions, grantResults)
        }

    }

    companion object {
        const val ACTION_OPEN_AOSP_CAMERA = "openCamera"
        const val REQUEST_IMAGE_CAPTURE = 1
        const val CAMERA_PERMISSION: String = Manifest.permission.CAMERA
        const val REQ_CODE_PERM_CAMERA = 200

        fun showPickedPhotoInfo(context: Context, contentUri: Uri) {
            if (BuildConfig.DEBUG) {
                Logger.d(
                    "URI: $contentUri\n" +
                            "  getPath: ${contentUri.path}\n" +
                            "  getScheme: ${contentUri.scheme}\n" +
                            "  getLastPathSegment: ${contentUri.lastPathSegment}\n" +
                            "  File Size: ${getReadableFileSizeFromContentUri(context, contentUri)}"
                )
            } else {
                Logger.d("URI: $contentUri")
            }
        }

        private fun getReadableFileSizeFromContentUri(
            context: Context, contentUri: Uri
        ): String? = try {
            getBytes(context, contentUri)
        } catch (e: IOException) {
            Logger.w("Failed to get contentUri bytes.")
            null
        }?.let { Formatter.formatFileSize(context, it.size.toLong()) } ?: "Unknown"

        @Throws(IOException::class)
        private fun getBytes(context: Context, contentUri: Uri): ByteArray? = FileInputStream(
            context.contentResolver.openFileDescriptor(contentUri, "r")?.fileDescriptor
        ).use { inputStream ->
            ByteArrayOutputStream().use { byteBuffer ->
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    byteBuffer.write(buffer, 0, len)
                }
                byteBuffer.toByteArray()
            }
        }
    }
}