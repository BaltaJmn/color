package com.baltajmn.color.data

import android.content.Context

/** Set once from MainActivity and the receivers. Storage needs a Context and common code cannot hold one. */
object AndroidContext {
    lateinit var value: Context
        private set

    fun init(context: Context) {
        if (!::value.isInitialized) value = context.applicationContext
    }
}
