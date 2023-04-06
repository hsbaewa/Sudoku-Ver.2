package kr.co.hs.sudoku.extension

import androidx.preference.Preference
import coil.ImageLoader
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest

object CoilExt {
    fun Preference.loadIcon(
        data: Any?,
        imageLoader: ImageLoader = context.imageLoader,
        builder: ImageRequest.Builder.() -> Unit = {}
    ): Disposable {
        val request = ImageRequest.Builder(context)
            .data(data)
            .target { icon = it }
            .apply(builder)
            .build()
        return imageLoader.enqueue(request)
    }
}