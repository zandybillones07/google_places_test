package com.example.myapplication.model


import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StoreInfo(
    @Expose @SerializedName("g") val g: String? = null,
    @Expose @SerializedName("l") val l: List<Double?>? = null,
    @Expose @SerializedName("name") val name: String? = null,
    @Expose @SerializedName("visited") val visited: Int? = null,
    @Expose @SerializedName("details") val details: String? = null,
    @Expose @SerializedName("menus") val menus: String? = null
) : Parcelable