package com.example.storyapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat


class ValidatingEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : AppCompatEditText(context, attrs) {

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                val currentInputType = inputType

                when (currentInputType) {
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT -> {
                        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
                        if (!isValid) {
                            showErrorIcon("Email tidak valid")
                        } else {
                            clearErrorIcon()
                        }
                    }

                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                        when {
                            input.length < 8 -> showErrorIcon("Password harus lebih dari 8 karakter")
                            input.contains(" ") -> showErrorIcon("Password tidak boleh menggunakan spasi")
                            else -> clearErrorIcon()
                        }
                    }

                    else -> {
                        when {
                            input.isEmpty() -> showErrorIcon("Tidak boleh kosong")
                            input.contains(" ") -> showErrorIcon("Tidak boleh mengandung spasi")
                            else -> clearErrorIcon()
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showErrorIcon(message: String) {
        error = message
        setCompoundDrawablesWithIntrinsicBounds(
            null, null, resizeDrawable(R.drawable.baseline_error), null
        )
    }

    private fun clearErrorIcon() {
        error = null
        setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun resizeDrawable(drawableRes: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            val scaledSize = resources.getDimensionPixelSize(R.dimen.error_icon_size) // Define size in dimens.xml
            drawable.setBounds(0, 0, scaledSize, scaledSize)
        }
        return drawable
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setPaddingRelative(20, 0, 20, 0)
    }
}







