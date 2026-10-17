package com.rafi.blackholetheme

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 32, 28, 32)
            setBackgroundColor(Color.rgb(3, 4, 7))
        }
        val title = TextView(this).apply {
            text = "BLACK HOLE  /  PREMIUM"
            textSize = 23f
            setTextColor(Color.rgb(255, 177, 91))
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Cinematic live wallpaper\nAMOLED black • amber accretion • drifting particles"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 28)
        }
        val preview = Button(this).apply {
            text = "Preview & Set Wallpaper"
            setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, BlackHoleWallpaperService::class.java))
                }
                startActivity(intent)
            }
        }
        val note = TextView(this).apply {
            text = "After preview, choose Home screen or Home and lock screens if your phone offers that option."
            textSize = 13f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        root.addView(title, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2))
        root.addView(subtitle, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2))
        root.addView(preview, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2))
        root.addView(note, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, -2))
        setContentView(root)
    }
}
