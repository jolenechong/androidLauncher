package com.example.launcher

import android.content.*
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.launcher.database.AppViewerApplication
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {

    private lateinit var apps: MutableList<App>
    private lateinit var listView: ListView
    private lateinit var noteView: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set note
        noteView = view.findViewById(R.id.note)
        noteView.setText(context?.let { AppViewerApplication().retrieveNote(it) })

        // initialize a new intent filter instance for battery level
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        activity?.registerReceiver(receiver,filter)

        // initialise listview and date and display date
        view.findViewById<TextView>(R.id.date).text = getDate()
        listView = view.findViewById(R.id.selectedAppsList)
        refreshList()

        listView.setOnItemClickListener(){ parent, view, position, id ->
            try {
                val pm: PackageManager = activity?.packageManager!!
                var intent = pm.getLaunchIntentForPackage(apps[position].packageName)
                if (intent == null) throw PackageManager.NameNotFoundException()
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                startActivity(intent)
            } catch (e: PackageManager.NameNotFoundException) {
                Toast.makeText(context,"App Not Available", Toast.LENGTH_SHORT).show()
            }
        }

        listView.setOnItemLongClickListener(AdapterView.OnItemLongClickListener { parent, view, pos, id ->
            // delete from db
            AppViewerApplication().deleteFrmDatabase(apps[pos].packageName, requireContext())

            refreshList()
            Toast.makeText(context, "App Removed From Home Screen", Toast.LENGTH_SHORT).show()

            true
        })

        noteView.addTextChangedListener{
            context?.let { it1 ->
                AppViewerApplication().insertNoteEntry(noteView.text.toString(),
                    it1
                )
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // refresh list on resume
        refreshList()
    }

    fun refreshList(){
        var listJob = GlobalScope.async(Dispatchers.Default) {
            Log.d("BOOP","I'm working in thread ${Thread.currentThread().name}")

            var appList = getSelectedApps()

            appList

        }

        GlobalScope.launch(Dispatchers.Main) {
            Log.d("BOOP","I'm working in thread ${Thread.currentThread().name}")
            apps = listJob.await()
            listView.adapter = AppsAdapter(requireActivity(), apps)

        }
    }

    fun getSelectedApps() : MutableList<App> {
        // get list of all the apps installed
        val pm: PackageManager = activity?.packageManager!!
        val installedApps: MutableList<App> = ArrayList()

        var appIDs = AppViewerApplication().retrieveAll(requireContext())
        for (app in appIDs)
        {
            try {
                val saveApp = App(
                    pm.getApplicationInfo(app,0).loadLabel(pm) as String,
                    pm.getApplicationIcon(app),
                    app
                )
                installedApps.add(saveApp)

            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        // sort alphabetically
        installedApps.sortBy { it.name }

        return installedApps
    }

    fun getDate(): String{
        var date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance() //or use getDateInstance()
        val formatedDate = formatter.format(date)

        return formatedDate
    }

    // extension property to get current battery charge percentage from intent
    val Intent.currentBatteryCharge:Float
        get() {
            // integer containing the maximum battery level
            val scale = getIntExtra(
                BatteryManager.EXTRA_SCALE, -1
            )

            //  integer field containing the current battery
            //  level, from 0 to EXTRA_SCALE
            val level = getIntExtra(
                BatteryManager.EXTRA_LEVEL, -1
            )

            // return current battery charge percentage
            return level * 100 / scale.toFloat()
        }

    private val receiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.apply {
                view?.findViewById<TextView>(R.id.battery)?.text = "$currentBatteryCharge%"
            }
        }
    }

}