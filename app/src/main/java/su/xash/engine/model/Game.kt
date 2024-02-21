package su.xash.engine.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import org.spongycastle.math.raw.Mod
import su.xash.engine.XashActivity


class Game(
    val ctx: Context,
    val basedir: DocumentFile,
    var installed: Boolean = true,
    val dbEntry: ModDatabase.Entry? = null
) {
    private var iconName = "game.ico"
    var title = "Unknown Game"
    var icon: Bitmap? = null
    var cover: Bitmap? = null

    private val pref = ctx.getSharedPreferences(basedir.name, Context.MODE_PRIVATE)

    init {
        basedir.findFile("gameinfo.txt")?.let {
            parseGameInfo(it)
        } ?: basedir.findFile("liblist.gam")?.let { parseGameInfo(it) }

        basedir.findFile(iconName)
            ?.let { icon = MediaStore.Images.Media.getBitmap(ctx.contentResolver, it.uri) }

        try {
            cover = BackgroundBitmap.createBackground(ctx, basedir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startEngine(ctx: Context) {
        ctx.startActivity(Intent(ctx, XashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("gamedir", basedir.name)
            putExtra("argv", pref.getString("arguments", "-dev 2 -log"))
            putExtra("usevolume", pref.getBoolean("use_volume_buttons", false))
            putExtra("gamelibdir", getGameLibDir(ctx))
            putExtra("package", getPackageName(ctx))
        })
    }

    private fun parseGameInfo(file: DocumentFile) {
        ctx.contentResolver.openInputStream(file.uri).use { inputStream ->
            inputStream?.bufferedReader().use { reader ->
                reader?.forEachLine {
                    val tokens = it.split("\\s+".toRegex(), limit = 2)
                    if (tokens.size >= 2) {
                        val k = tokens[0]
                        val v = tokens[1].trim('"')

                        if (k == "title" || k == "game") title = v
                        if (k == "icon") iconName = v
                    }
                }
            }
        }
    }

    private fun getPackageName(ctx: Context): String? {
        return pref.getString("package_name", ctx.packageName)
    }

    private fun getGameLibDir(ctx: Context): String? {
        val pkgName = getPackageName(ctx)
        if (pkgName != null) {
            val pkgInfo: PackageInfo = try {
                ctx.packageManager.getPackageInfo(pkgName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                ctx.startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkgName")
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                return null
            }
            return pkgInfo.applicationInfo.nativeLibraryDir
        }
        return ctx.applicationInfo.nativeLibraryDir
    }

    fun getPreferences(): SharedPreferences {
        return pref;
    }

    companion object {
        fun getGames(ctx: Context, file: DocumentFile, ignoreRoot: Boolean = false): List<Game> {
            val games = mutableListOf<Game>()

            if (checkIfGamedir(file) && !ignoreRoot) {
                games.add(Game(ctx, file))
            } else {
                file.listFiles().forEach {
                    if (it.isDirectory) {
                        if (checkIfGamedir(it)) {
                            games.add(Game(ctx, it))
                        }
                    }
                }
            }

            return games
        }

        fun checkIfGamedir(file: DocumentFile): Boolean {
            file.findFile("liblist.gam")?.let { return true }
            file.findFile("gameinfo.txt")?.let { return true }
            return false
        }
    }
}