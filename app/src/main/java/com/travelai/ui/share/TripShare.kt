package com.travelai.ui.share

import android.content.Context
import android.content.Intent

fun shareTripText(
    context: Context,
    exportText: String
) {
    if (exportText.isBlank()) return

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, exportText)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "Chia sẻ lịch trình")
    )
}
