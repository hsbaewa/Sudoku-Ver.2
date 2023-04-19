package kr.co.hs.sudoku.feature.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kr.co.hs.sudoku.R
import kr.co.hs.sudoku.extension.platform.TextInputEditTextExt.setVisibilityWithLayout
import kr.co.hs.sudoku.extension.platform.TextViewExtension.clearFocusWithKeyboard
import kr.co.hs.sudoku.extension.platform.TextViewExtension.requestFocusWithKeyboard
import kr.co.hs.sudoku.model.user.ProfileEntity

class ProfileDialog(
    context: Context,
    private val profileEntity: ProfileEntity,
    private val editable: Boolean = false,
    private val onSubmit: ((ProfileEntity) -> Unit)? = null
) : BottomSheetDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_profile_dialog)
        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val btnPhoto = findViewById<ImageButton>(R.id.btnPhoto)
        val tvDisplayName = findViewById<TextView>(R.id.tvDisplayName)
        val btnDisplayName = findViewById<ImageButton>(R.id.btnDisplayName)
        val tvStatusMessage = findViewById<TextView>(R.id.tvStatusMessage)
        val btnStatusMessage = findViewById<ImageButton>(R.id.btnStatusMessage)

        val editDisplayName = findViewById<TextInputEditText>(R.id.editDisplayName)
        val editStatusMessage = findViewById<TextInputEditText>(R.id.editStatusMessage)

        profileEntity.run {
            ivPhoto?.setupUIPhoto(iconUrl)
            btnPhoto?.setupUIPhoto {}

            tvDisplayName?.setupUIDisplayName(displayName)
            btnDisplayName?.setupUIDisplayName {
                editDisplayName?.setVisibilityWithLayout(View.VISIBLE)
                editDisplayName?.requestFocusWithKeyboard()
            }

            tvStatusMessage?.setupUIStatusMessage(message)
            btnStatusMessage?.setupUIStatusMessage {
                editStatusMessage?.setVisibilityWithLayout(View.VISIBLE)
                editStatusMessage?.requestFocusWithKeyboard()
            }

            editDisplayName?.setupUIDisplayName({ _, focus ->
                if (focus) {
                    tvDisplayName?.visibility = View.GONE
                    btnDisplayName?.visibility = View.GONE
                    editDisplayName.setVisibilityWithLayout(View.VISIBLE)
                    editDisplayName.setText(displayName)
                    editDisplayName.setSelection(displayName.length)
                } else {
                    tvDisplayName?.visibility = View.VISIBLE
                    btnDisplayName?.visibility = if (editable) View.VISIBLE else View.GONE
                    editDisplayName.setVisibilityWithLayout(View.GONE)
                }
            }, {
                displayName = it.toString()
                tvDisplayName?.setText(it)
            })
            editStatusMessage?.setupUIStatusMessage({ _, focus ->
                if (focus) {
                    tvStatusMessage?.visibility = View.GONE
                    btnStatusMessage?.visibility = View.GONE
                    editStatusMessage.setVisibilityWithLayout(View.VISIBLE)
                    editStatusMessage.setText(message)
                    editStatusMessage.setSelection(message?.length ?: 0)
                } else {
                    tvStatusMessage?.visibility = View.VISIBLE
                    btnStatusMessage?.visibility = if (editable) View.VISIBLE else View.GONE
                    editStatusMessage.setVisibilityWithLayout(View.GONE)
                }
            }, {
                message = it.toString()
                tvStatusMessage?.setText(it)
            })
        }


        val btnEditSubmit = findViewById<MaterialButton>(R.id.btnEditSubmit)
        btnEditSubmit?.setupUISubmitButton {
            dismiss()
            onSubmit?.invoke(profileEntity)
        }
    }

    private fun ImageView.setupUIPhoto(url: String?) {
        visibility = View.VISIBLE
        load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
            val errorIcon = ContextCompat.getDrawable(context, R.drawable.games_controller)
            error(errorIcon)
        }
    }

    private inline fun ImageButton.setupUIPhoto(crossinline onClick: (ImageButton) -> Unit?) {
        visibility = if (editable) View.VISIBLE else View.GONE
        setOnClickListener { onClick(it as ImageButton) }
    }

    private fun TextView.setupUIDisplayName(displayName: String) {
        visibility = View.VISIBLE
        text = displayName
    }

    private fun ImageButton.setupUIDisplayName(onClick: (ImageButton) -> Unit?) {
        visibility = if (editable) View.VISIBLE else View.GONE
        setOnClickListener { onClick(it as ImageButton) }
    }


    private fun TextView.setupUIStatusMessage(statusMessage: String?) {
        visibility = View.VISIBLE
        text = statusMessage
    }

    private fun ImageButton.setupUIStatusMessage(onClick: (ImageButton) -> Unit?) {
        visibility = if (editable) View.VISIBLE else View.GONE
        setOnClickListener { onClick(it as ImageButton) }
    }

    private inline fun TextInputEditText.setupUIDisplayName(
        crossinline onChangeFocus: (TextInputEditText, Boolean) -> Unit?,
        crossinline onActionDone: (CharSequence) -> Unit?
    ) {
        setVisibilityWithLayout(View.GONE)
        onFocusChangeListener =
            View.OnFocusChangeListener { view, b -> onChangeFocus(view as TextInputEditText, b) }
        setOnEditorActionListener { textView, i, _ ->
            return@setOnEditorActionListener if (i == EditorInfo.IME_ACTION_DONE) {
                textView.clearFocusWithKeyboard()
                onActionDone(textView.text)
                true
            } else false
        }
    }

    private inline fun TextInputEditText.setupUIStatusMessage(
        crossinline onChangeFocus: (TextInputEditText, Boolean) -> Unit?,
        crossinline onActionDone: (CharSequence) -> Unit?
    ) {
        setVisibilityWithLayout(View.GONE)
        onFocusChangeListener =
            View.OnFocusChangeListener { view, b -> onChangeFocus(view as TextInputEditText, b) }
        setOnEditorActionListener { textView, i, _ ->
            return@setOnEditorActionListener if (i == EditorInfo.IME_ACTION_DONE) {
                textView.clearFocusWithKeyboard()
                onActionDone(textView.text)
                true
            } else false
        }
    }

    private inline fun MaterialButton.setupUISubmitButton(
        crossinline onClick: (MaterialButton) -> Unit?
    ) {
        visibility = if (editable) View.VISIBLE else View.GONE
        setOnClickListener {
            onClick(it as MaterialButton)
        }
    }

}