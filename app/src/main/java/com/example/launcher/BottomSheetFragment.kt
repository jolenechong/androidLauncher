package com.example.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.launcher.database.AppViewerApplication
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetFragment: BottomSheetDialogFragment() {

    private var name: String = "name"
    private var package_name: String = "package name"

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // get arguments from bundle passed from appdrawerfragment
        arguments?.getString("FRAG_NAME")?.let {
            name = it
        }
        arguments?.getString("FRAG_PACKAGE_NAME")?.let {
            package_name = it
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get views
        var appNameView : TextView = view.findViewById(R.id.fragAppName)
        var addToHomeBtn : Button = view.findViewById(R.id.addToHome)
        var appInfoBtn : Button = view.findViewById(R.id.appInfo)
        var uninstallBtn : Button = view.findViewById(R.id.uninstall)

        appNameView.text = name

        addToHomeBtn.setOnClickListener{
            // add to db
            AppViewerApplication().addToDatabase(
                name,
                package_name, requireContext())

            Toast.makeText(context,"${name} added to Home", Toast.LENGTH_SHORT).show()
        }

        appInfoBtn.setOnClickListener{
            // show app info using package name
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${package_name}")
            startActivity(intent)
        }

        uninstallBtn.setOnClickListener{
            // uninstall app using package name
            val intent = Intent(Intent.ACTION_DELETE)
            intent.data = Uri.parse("package:${package_name}")
            startActivity(intent)
        }
    }
}