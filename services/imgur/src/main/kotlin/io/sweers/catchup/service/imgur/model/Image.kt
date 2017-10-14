/*
 * Copyright (c) 2017 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sweers.catchup.service.imgur.model

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import com.google.auto.value.AutoValue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.sweers.catchup.util.getSystemService
import org.threeten.bp.Instant


@AutoValue
internal abstract class Image {

  abstract fun id(): String
  abstract fun title(): String
  abstract fun datetime(): Instant
  abstract fun cover(): String?
  abstract fun link(): String
  abstract fun downs(): Int?
  abstract fun type(): String?
  abstract fun ups(): Int?
  abstract fun score(): Int?
  @Json(name = "account_url") abstract fun accountUrl(): String?
  @Json(name = "account_id") abstract fun accountId(): String?

  fun resolveScore(): Int {
    score()?.let { return it }
    ups()?.let { ups ->
      downs()?.let {
        return ups - it
      }
    }
    return 0
  }

  fun resolveClickLink() = link()

  fun resolveDisplayLink(size: String = "l"): String {
    cover()?.let { return "https://i.imgur.com/$it$size.webp" }
    val type = resolveType()
    return "https://i.imgur.com/${id()}$size.$type"
  }

  private fun resolveType(): String? {
    val type = type()
    return when (type) {
      null -> null
      "image/gif" -> "gif"
      else -> "webp"
    }
  }

  companion object {
    /**
     * Not used yet as I'm not sure where to call this
     */
    fun resolveBestSize(context: Context): String {
//      t	Small Thumbnail	160x160	Yes
//      m	Medium Thumbnail	320x320	Yes
//      l	Large Thumbnail	640x640	Yes
//      h	Huge Thumbnail	1024x1024	Yes
      val smallLayout = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_SMALL
      val lowRam = context.getSystemService<ActivityManager>().isLowRamDevice
      return if (lowRam) {
        if (smallLayout) {
          "t"
        } else {
          "m"
        }
      } else {
        "l"
      }
    }

    @JvmStatic
    fun jsonAdapter(moshi: Moshi): JsonAdapter<Image> =
        AutoValue_Image.MoshiJsonAdapter(moshi)
  }

}
