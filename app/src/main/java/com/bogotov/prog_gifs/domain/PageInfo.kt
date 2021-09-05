package com.bogotov.prog_gifs.domain

import android.os.Parcel
import android.os.Parcelable

class PageInfo(val resourceId: Int, val pageSection: PageSection) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt(), PageSection.valueOf(parcel.readString()!!))

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeInt(resourceId)
            writeString(pageSection.name)
        }
    }

    companion object CREATOR : Parcelable.Creator<PageInfo> {
        override fun createFromParcel(parcel: Parcel): PageInfo {
            return PageInfo(parcel)
        }

        override fun newArray(size: Int): Array<PageInfo?> {
            return arrayOfNulls(size)
        }
    }
}
