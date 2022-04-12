package io.legado.app.ui.book.read.page.provider

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookSource
import io.legado.app.help.BookHelp
import io.legado.app.help.glide.ImageLoader
import io.legado.app.model.localBook.EpubFile
import io.legado.app.utils.BitmapUtils
import io.legado.app.utils.FileUtils
import io.legado.app.R
import kotlinx.coroutines.runBlocking
import splitties.init.appCtx
import java.io.FileOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object ImageProvider {

    fun getImage(
        book: Book,
        chapterIndex: Int,
        src: String,
        bookSource: BookSource?,
        onUi: Boolean = false,
    ): Bitmap? {
        val vFile = BookHelp.getImage(book, src)
        if (!vFile.exists()) {
            if (book.isEpub()) {
                EpubFile.getImage(book, src)?.use { input ->
                    val newFile = FileUtils.createFileIfNotExist(vFile.absolutePath)
                    FileOutputStream(newFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else if (!onUi) {
                runBlocking {
                    BookHelp.saveImage(bookSource, book, src)
                }
            }
        }
        return ImageLoader.loadBitmap(appCtx, vFile.absolutePath)
            .error(R.drawable.image_loading_error)
            .listener(glideListener)
            .submit()
            .get()
    }

    private val glideListener by lazy {
        object : RequestListener<Bitmap> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                File(model as String).delete()
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        }
    }

}
