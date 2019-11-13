package com.netzwelt.studentmodule.adaptors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.autohub.skln.listeners.ItemClickListener
import com.autohub.skln.models.User
import com.autohub.skln.utills.CommonUtils
import com.autohub.skln.utills.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.netzwelt.studentmodule.R
import com.netzwelt.studentmodule.databinding.ExploreRowBinding

/**
 * Created by Vt Netzwelt
 */

class ExploreAdaptor(var context: Context, var mItemClickListener: ItemClickListener<User>)
    : RecyclerView.Adapter<ExploreAdaptor.Holder>() {

    private var userList: List<User> = ArrayList()
    private var mCurrentLocation: Location? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val exploreRowBinding: ExploreRowBinding =
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.explore_row, parent, false
                )

        return Holder(exploreRowBinding)
    }


    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.exploreRowBinding.itemClickListener = mItemClickListener
        userList[position].let {
            with(holder.exploreRowBinding)
            {

                if (mCurrentLocation != null) txtdistance.text = "${it.distance} Km"

                user = it
                tutorname.text = """${it.firstName} ${it.lastName}"""
                txtclassprice.text = """${it.rate} / ${it.noOfClasses} PER ${it.paymentDuration}"""


                val splitarray = it.classesToTeach.split(",")
                if (splitarray.isNotEmpty()) {
                    val stringBuilder = StringBuilder(splitarray.size)
                    for (i in splitarray.indices) {
                        if (i == splitarray.size - 1) {
                            stringBuilder.append(splitarray[i] + CommonUtils.getClassSuffix(splitarray[i].toInt()))

                        } else {
                            stringBuilder.append(splitarray[i] + CommonUtils.getClassSuffix(splitarray[i].toInt()) + " - ")
                        }
                    }
                    txtgrades.text = stringBuilder.toString()


                } else {
                    txtgrades.text = it.classesToTeach

                }



                txtclasstype.text = it.classType
                txtsubjects.text = it.subjectsToTeach.replace(",", " | ")

                if (!TextUtils.isEmpty(it.pictureUrl)) {
                    val pathReference1 = FirebaseStorage.getInstance().reference.child(it.pictureUrl)
                    val options = RequestOptions()
                    options.transforms(MultiTransformation(CenterCrop(), RoundedCornersTransformation(context, CommonUtils.convertDpToPixel(6f, context).toInt(), 0)))
                    options.placeholder(R.drawable.dummyexploreimage)
                    Glide.with(context).load(pathReference1)
                            .apply(options).into(imgprofile)

                }
            }
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setData(userList: List<User>, mCurrentLocation: Location?) {
        this.userList = userList
        this.mCurrentLocation = mCurrentLocation
        notifyDataSetChanged()
    }

    inner class Holder(var exploreRowBinding: ExploreRowBinding) :
            RecyclerView.ViewHolder(exploreRowBinding.root) {
        fun bind() {
        }
    }


}