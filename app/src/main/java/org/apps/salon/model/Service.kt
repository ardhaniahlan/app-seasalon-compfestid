package org.apps.salon.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Service(
    val id: String? = null,
    val serviceName: String? = null,
    val serviceSlogan: String? = null
): Parcelable

