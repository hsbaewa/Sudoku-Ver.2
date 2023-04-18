package kr.co.hs.sudoku.extension

import android.content.Context
import coil.ImageLoader

object CoilExt {
    val Context.appImageLoader: ImageLoader
        get() = ImageLoader.Builder(this).build()
}