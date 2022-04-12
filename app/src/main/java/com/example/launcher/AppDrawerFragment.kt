package com.example.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.collections.ArrayList


class AppDrawerFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var apps: MutableList<App> // dont change
    private lateinit var filteredApps: MutableList<App> // change to filter
    private lateinit var searchField : EditText
    private lateinit var imgr : InputMethodManager
    private lateinit var pm : PackageManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app_drawer, container, false)
    }

    override fun onResume() {
        super.onResume()
        // focus on search and bring up keyboard and set text to nth
        searchField.setText("")
        searchField.requestFocus()
        imgr.showSoftInput(searchField,0)
    }

    override fun onStart() {
        super.onStart()
        // refresh list
        apps = getallapps()
        refreshList(apps)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialise all my variables
        listView = view.findViewById(R.id.listview)
        imgr = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        searchField = view.findViewById(R.id.search)
        pm = activity?.packageManager!!

        // handle search
        searchField.addTextChangedListener {
            val match =
                apps.filter {
                    it.name.lowercase(Locale.getDefault()).contains(searchField.text.toString().lowercase(Locale.getDefault()))
                }
            refreshList(match.toMutableList())
        }

        // open app on click with package manager
        listView.isClickable = true
        listView.setOnItemClickListener(){ parent, view, position, id ->

            try {
                var intent = pm.getLaunchIntentForPackage(filteredApps[position].packageName)
                if (intent == null) throw PackageManager.NameNotFoundException()
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                startActivity(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(context,"App Not Available", Toast.LENGTH_SHORT).show()
            }
        }

        // open fragment on long click
        listView.setOnItemLongClickListener(OnItemLongClickListener { parent, view, pos, id ->

            val fragment = BottomSheetFragment();

            fragment.apply {
                arguments = Bundle().apply {
                    putString("FRAG_NAME", filteredApps[pos].name)
                    putString("FRAG_PACKAGE_NAME", filteredApps[pos].packageName)
                }
            }

            activity?.let { fragment.show(it.supportFragmentManager, "fragment_tag") }
            true
        })

        // hide keyboard on scroll
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {}

            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState != 0)
                {
                    imgr.hideSoftInputFromWindow(view.windowToken,0)
                }
            }
        })
    }

    fun refreshList(appsToShow: MutableList<App>)
    {
        filteredApps = appsToShow
        listView.adapter = AppsAdapter(requireActivity(), appsToShow)
    }

    fun getallapps() : MutableList<App>{
        // get list of all the apps installed
        val main = Intent(Intent.ACTION_MAIN, null)
        main.addCategory(Intent.CATEGORY_LAUNCHER)
        val appsL = pm.queryIntentActivities(main, 0)

        val installedApps: MutableList<App> = ArrayList()

        for (app in appsL)
        {
            // get apps installed by users
            val saveApp = App(
                app.loadLabel(pm) as String,
                app.loadIcon(pm),
                app.activityInfo.packageName
                )
            installedApps.add(saveApp)
        }
        // sort alphabetically
        installedApps.sortBy { it.name }

        return installedApps
    }

}