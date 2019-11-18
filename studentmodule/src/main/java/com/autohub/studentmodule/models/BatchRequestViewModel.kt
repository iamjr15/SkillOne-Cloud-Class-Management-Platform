package com.autohub.studentmodule.models

import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.BaseObservable

data class BatchRequestViewModel(var batchRequestModel: BatchrequestModel? = BatchrequestModel(),

                                 val mUserType: String? = "",

                                 val mRequestId: String? = "") : BaseObservable(), Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(BatchrequestModel::class.java.classLoader),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(batchRequestModel, flags)
        parcel.writeString(mUserType)
        parcel.writeString(mRequestId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BatchRequestViewModel> {
        override fun createFromParcel(parcel: Parcel): BatchRequestViewModel {
            return BatchRequestViewModel(parcel)
        }

        override fun newArray(size: Int): Array<BatchRequestViewModel?> {
            return arrayOfNulls(size)
        }
    }
}