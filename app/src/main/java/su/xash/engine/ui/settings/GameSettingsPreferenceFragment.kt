package su.xash.engine.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import su.xash.engine.BuildConfig
import su.xash.engine.R
import su.xash.engine.model.Game

//    Intent intent = new Intent("su.xash.engine.MOD");
//                for (ResolveInfo info : context.getPackageManager()
//                        .queryIntentActivities(intent, PackageManager.GET_META_DATA)) {
//                        String packageName = info.activityInfo.applicationInfo.packageName;
//                        String gameDir = info.activityInfo.applicationInfo.metaData.getString(
//                        "su.xash.engine.gamedir");
//                        Log.d(TAG, "package = " + packageName + " gamedir = " + gameDir);
//                        }
class GameSettingsPreferenceFragment(val game: Game) : PreferenceFragmentCompat() {
    private fun readPackages(entries: MutableList<String>, entryValues: MutableList<String>) {
        val intent = Intent("su.xash.engine.MOD")
        requireContext().packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            .forEach {
                val name = requireContext().packageManager.getApplicationLabel(it.activityInfo.applicationInfo)
                entries.add(name.toString())
                entryValues.add(it.activityInfo.applicationInfo.packageName)
            }
    }

    private fun updateVisiblePreferences(separate: Boolean) {
        val packageList = findPreference<ListPreference>("package_name")!!
        val clientPackage = findPreference<ListPreference>("client_package")!!
        val serverPackage = findPreference<ListPreference>("server_package")!!

        if (separate) {
            packageList.isVisible = false
            clientPackage.isVisible = true
            serverPackage.isVisible = true
        } else {
            packageList.isVisible = true
            clientPackage.isVisible = false
            serverPackage.isVisible = false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = game.basedir.name;
        setPreferencesFromResource(R.xml.game_preferences, rootKey);

        val entries = mutableListOf(getString(R.string.app_name))
        val entryValues = mutableListOf(requireContext().packageName)
        readPackages(entries, entryValues)

        val packageList = findPreference<ListPreference>("package_name")!!
        packageList.entries = entries.toTypedArray()
        packageList.entryValues = entryValues.toTypedArray()

        if (packageList.value == null) {
            packageList.setValueIndex(0);
        }

        val separatePackages = findPreference<SwitchPreferenceCompat>("separate_libraries")!!
        updateVisiblePreferences(separatePackages.isChecked)
        separatePackages.setOnPreferenceChangeListener { _, newValue ->
            updateVisiblePreferences(newValue as Boolean)
            true
        }
    }
}