package com.travelai.data.model

data class TripProfile(
    val destination: String,
    val days: Int,
    val budget: String,
    val people: Int,
    val travelStyle: String,
    val transport: String,
    val note: String
)

fun TripProfile.toSessionTitle(): String {
    val trimmedDestination = destination.trim()
    val rawTitle = if (trimmedDestination.isBlank()) {
        "Chuyến đi mới"
    } else {
        "$days ngày $trimmedDestination"
    }

    return if (rawTitle.length <= SESSION_TITLE_MAX_LENGTH) {
        rawTitle
    } else {
        rawTitle.take(SESSION_TITLE_MAX_LENGTH).trimEnd()
    }
}

fun TripProfile.toInitialPrompt(): String = buildString {
    appendLine("Hãy lập lịch trình du lịch chi tiết theo profile sau:")
    appendLine("- Điểm đến: ${destination.trim()}")
    appendLine("- Số ngày: $days")
    appendLine("- Số người: $people")
    appendOptionalPromptLine("Ngân sách", budget)
    appendOptionalPromptLine("Phong cách", travelStyle)
    appendOptionalPromptLine("Phương tiện", transport)
    appendOptionalPromptLine("Ghi chú", note)
    appendLine()
    appendLine("Yêu cầu trả lời:")
    appendLine("- Lập đúng $days ngày, chia rõ Ngày 1, Ngày 2... và Sáng / Chiều / Tối.")
    appendLine("- Gợi ý địa điểm, món ăn, thời lượng, thứ tự di chuyển hợp lý.")
    appendLine("- Nếu có ngân sách, cân đối chi phí để phù hợp với ngân sách đó.")
    append("- Trả lời bằng tiếng Việt, thực tế, dễ làm theo.")
}

fun TripProfile.toPromptContext(): String = buildString {
    appendLine("Thông tin chuyến đi đang lưu:")
    appendLine("- Điểm đến: ${destination.trim()}")
    appendLine("- Số ngày: $days")
    appendLine("- Số người: $people")
    appendOptionalPromptLine("Ngân sách", budget)
    appendOptionalPromptLine("Phong cách", travelStyle)
    appendOptionalPromptLine("Phương tiện", transport)
    appendOptionalPromptLine("Ghi chú", note)
}

private fun StringBuilder.appendOptionalPromptLine(
    label: String,
    value: String
) {
    val trimmedValue = value.trim()
    if (trimmedValue.isNotBlank()) {
        appendLine("- $label: $trimmedValue")
    }
}

private const val SESSION_TITLE_MAX_LENGTH = 60
