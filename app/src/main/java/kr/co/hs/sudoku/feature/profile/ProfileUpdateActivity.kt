package kr.co.hs.sudoku.feature.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import kr.co.hs.sudoku.App
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.core.Activity
import kr.co.hs.sudoku.databinding.ActivityProfileUpdateBinding
import kr.co.hs.sudoku.extension.CoilExt.load
import kr.co.hs.sudoku.extension.Number.dp
import kr.co.hs.sudoku.extension.platform.ActivityExtension.isShowProgressIndicator
import kr.co.hs.sudoku.extension.platform.ContextExtension.getDrawableCompat

class ProfileUpdateActivity : Activity() {
    companion object {
        fun newIntent(context: Context) =
            Intent(context, ProfileUpdateActivity::class.java)

        fun start(context: Context) = context.startActivity(newIntent(context))
    }

    private val binding: ActivityProfileUpdateBinding
            by lazy { DataBindingUtil.setContentView(this, R.layout.activity_profile_update) }
    private val app: App by lazy { applicationContext as App }
    private val viewModel: UserProfileViewModel
            by viewModels { getUserProfileProviderFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.error.observe(this) { it.showErrorAlert() }
        viewModel.profile.observe(this) {
            with(binding.ivProfileIcon) {
                load(it?.iconUrl, errorIcon = context.getDrawableCompat(R.drawable.ic_person))
            }
            with(binding.editDisplayName.editableText) {
                clear()
                append(it?.displayName)
            }

            with(binding.editStatusMessage.editableText) {
                clear()
                append(it?.message)
            }
        }
        viewModel.isRunningProgress.observe(this) { isShowProgressIndicator = it }

        with(binding) {
            cardViewProfileIcon.setOnClickListener { showUpdateProfileImageDialog() }
            editDisplayName.doAfterTextChanged { viewModel.setDisplayName(it?.toString() ?: "") }
            editStatusMessage.doAfterTextChanged { viewModel.setMessage(it?.toString() ?: "") }
        }

        binding.btnUpdate.setOnClickListener {
            viewModel.updateUserInfo {
                setResult(RESULT_OK)
                navigateUpToParent()
            }
        }

        lifecycleScope.launch {
            withStarted { viewModel.requestLastUserProfile() }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateUpToParent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUpdateProfileImageDialog() = lifecycleScope.launch {
        val context = ContextThemeWrapper(
            this@ProfileUpdateActivity,
            R.style.Theme_HSSudoku2_TextInputLayout_OutlinedBox
        )
        val inputLayout = TextInputLayout(context)
            .apply { isHintEnabled = false }
        val editText = TextInputEditText(context)
            .apply { hint = getString(R.string.profile_image_input_hint) }
        inputLayout.addView(editText)

        val layout = LinearLayout(this@ProfileUpdateActivity)
        layout.addView(
            inputLayout,
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                .apply { setMargins(22.dp.toInt(), 0, 22.dp.toInt(), 0) }
        )

        MaterialAlertDialogBuilder(this@ProfileUpdateActivity)
            .setView(layout)
            .setTitle(R.string.profile_image_update_title)
            .setMessage(R.string.profile_image_update_message)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setPositiveButton(R.string.confirm) { _, _ ->
                binding.ivProfileIcon.load(
                    editText.text.toString(),
                    context.getDrawableCompat(R.drawable.ic_person),
                    onComplete = {
                        if (it == null) {
                            viewModel.setIconUrl(editText.text.toString())
                        } else {
                            showAlert(null, R.string.profile_image_url_invalid) {}
                        }
                    }
                )
            }
            .setCancelable(false)
            .show()
    }
}