package com.proptit.todohive.ui.home.task

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proptit.todohive.R
import com.proptit.todohive.data.local.AppDatabase
import com.proptit.todohive.data.local.entity.CategoryEntity
import com.proptit.todohive.databinding.ActivityCreateCategoriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateCategoriesBinding
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var pickedIconUriString: String? = null
    private var pickedColorHex: String = "#6C63FF"

    private val activityResultPickIcon = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pickedIconUriString = uri?.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPreviewColor(pickedColorHex)
        setUpWindowInsets()

        binding.btnPickIcon.setOnClickListener {
            activityResultPickIcon.launch("image/*")
        }

        binding.btnPickColor.setOnClickListener {
            showHsvColorPickerDialog()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnCreate.setOnClickListener {
            val categoryName: String = binding.etName.text?.toString()?.trim().orEmpty()
            if (categoryName.isEmpty()) {
                binding.tilName.error = "Please enter category name"
                return@setOnClickListener
            }
            binding.tilName.error = null

            val prefs = getSharedPreferences("app", MODE_PRIVATE)
            val ownerUserId: Long? = prefs.getLong("current_user_id", 0L).takeIf { it > 0L }
                ?: run {
                    throw IllegalStateException("No logged-in user. Cannot create personal category.")
                }

            ioScope.launch {
                val categoryId: Long = AppDatabase.get(applicationContext)
                    .categoryDao()
                    .upsert(
                        CategoryEntity(
                            name = categoryName,
                            icon = pickedIconUriString,
                            color_hex = pickedColorHex,
                            owner_user_id = ownerUserId
                        )
                    )
                setResult(
                    Activity.RESULT_OK,
                    Intent()
                        .putExtra(EXTRA_CATEGORY_ID, categoryId)
                        .putExtra(EXTRA_CATEGORY_NAME, categoryName)
                        .putExtra(EXTRA_CATEGORY_COLOR, pickedColorHex)
                )
                finish()
            }
        }
    }

    private fun setUpWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setPreviewColor(hex: String) {
        val colorInt: Int = Color.parseColor(hex)
        binding.viewColorPreview.backgroundTintList = ColorStateList.valueOf(colorInt)
    }

    private fun showHsvColorPickerDialog() {
        val inflater: LayoutInflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_color_picker, null)
        val previewView: android.view.View = dialogView.findViewById(R.id.preview)
        val seekBarHue: android.widget.SeekBar = dialogView.findViewById(R.id.seekHue)
        val seekBarSaturation: android.widget.SeekBar = dialogView.findViewById(R.id.seekSat)
        val seekBarValue: android.widget.SeekBar = dialogView.findViewById(R.id.seekVal)
        val textViewHex: android.widget.TextView = dialogView.findViewById(R.id.tvHex)

        val hsvValues: FloatArray = FloatArray(3)
        Color.colorToHSV(Color.parseColor(pickedColorHex), hsvValues)

        seekBarHue.max = 360
        seekBarSaturation.max = 100
        seekBarValue.max = 100

        seekBarHue.progress = hsvValues[0].toInt()
        seekBarSaturation.progress = (hsvValues[1] * 100).toInt()
        seekBarValue.progress = (hsvValues[2] * 100).toInt()

        fun currentColorInt(): Int = Color.HSVToColor(floatArrayOf(hsvValues[0], hsvValues[1], hsvValues[2]))
        fun hexOf(colorInt: Int): String = String.format("#%06X", 0xFFFFFF and colorInt)
        fun applyColor() {
            val colorInt: Int = currentColorInt()
            previewView.setBackgroundColor(colorInt)
            textViewHex.text = hexOf(colorInt)
        }

        val listener = object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (seekBar?.id == R.id.seekHue) hsvValues[0] = progress.toFloat()
                if (seekBar?.id == R.id.seekSat) hsvValues[1] = progress / 100f
                if (seekBar?.id == R.id.seekVal) hsvValues[2] = progress / 100f
                applyColor()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        }

        seekBarHue.setOnSeekBarChangeListener(listener)
        seekBarSaturation.setOnSeekBarChangeListener(listener)
        seekBarValue.setOnSeekBarChangeListener(listener)
        applyColor()

        MaterialAlertDialogBuilder(this)
            .setTitle("Pick a color")
            .setView(dialogView)
            .setPositiveButton("OK") { dialog, _ ->
                val colorInt: Int = currentColorInt()
                val colorHex: String = hexOf(colorInt)
                pickedColorHex = colorHex
                setPreviewColor(colorHex)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        const val EXTRA_CATEGORY_ID: String = "extra_category_id"
        const val EXTRA_CATEGORY_NAME: String = "extra_category_name"
        const val EXTRA_CATEGORY_COLOR: String = "extra_category_color"

        fun createIntent(context: Context): Intent {
            return Intent(context, CreateCategoryActivity::class.java)
        }
    }
}