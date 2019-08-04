/*
 * Copyright (C) 2019. Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("NOTHING_TO_INLINE")

package io.sweers.catchup

import android.app.ActivityManager
import android.content.Context
import androidx.annotation.Keep
import androidx.core.app.ActivityManagerCompat
import androidx.core.content.getSystemService
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * Configure Glide to set desired image quality.
 */
@Excludes(OkHttpLibraryGlideModule::class) // Using a custom InstanceBasedOkHttpLibraryGlideModule
@Keep
@GlideModule
class GlideConfiguration : AppGlideModule() {

  override fun applyOptions(context: Context, builder: GlideBuilder) {
    // Prefer higher quality images unless we're on a low RAM device
    val isLowRamDevice = context.getSystemService<ActivityManager>()?.let {
      ActivityManagerCompat.isLowRamDevice(it)
    } ?: true
    builder.setDefaultRequestOptions(RequestOptions()
        .format(if (isLowRamDevice) DecodeFormat.PREFER_RGB_565 else DecodeFormat.PREFER_ARGB_8888)
        .disallowHardwareConfig() // So Palette can work
    )
  }

  override fun isManifestParsingEnabled() = false
}
