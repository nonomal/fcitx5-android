package org.fcitx.fcitx5.android.ui.setup

import android.content.Context
import androidx.core.text.HtmlCompat
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.utils.InputMethodUtil

enum class SetupPage {
    Enable, Select;

    fun getHintText(context: Context) = context.getString(
        when (this) {
            Enable -> R.string.enable_ime_hint
            Select -> R.string.select_ime_hint
        }, context.getString(R.string.app_name)
    ).let { HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY) }

    fun getButtonText(context: Context) = context.getText(
        when (this) {
            Enable -> R.string.enable_ime
            Select -> R.string.select_ime
        }
    )

    fun getButtonAction(context: Context) = when (this) {
        Enable -> InputMethodUtil.startSettingsActivity(context)
        Select -> InputMethodUtil.showSelector(context)
    }

    fun isDone() = when (this) {
        Enable -> InputMethodUtil.isEnabled()
        Select -> InputMethodUtil.isSelected()
    }

    companion object {
        private val values = values()
        fun valueOf(value: Int) = values[value]
        fun SetupPage.isLastPage() = this == values.last()
        fun Int.isLastPage() = this == values.size - 1
        fun hasUndonePage() = values.any { !it.isDone() }
        fun firstUndonePage() = values.firstOrNull { !it.isDone() }
    }
}