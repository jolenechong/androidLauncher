package com.example.launcher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class AppsAdapter(context: Context, appList: MutableList<App>) : BaseAdapter(){

    private val mContext : Context = context
    private val appList = appList

    override fun getCount(): Int {
        return appList.size
    }

    override fun getItem(position: Int): Any {
        return appList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, p2: ViewGroup?): View {
        val inflater : LayoutInflater = LayoutInflater.from(mContext)
        val view : View = inflater.inflate(R.layout.app_list_item, null)

        // get views
        val nameTV : TextView = view.findViewById(R.id.app_name)
        val iconTV : ImageView = view.findViewById(R.id.app_icon)

        // fill views with icon and name of app
        nameTV.text = appList[position].name
        iconTV.setImageDrawable(appList[position].icon)

        return view
    }

}