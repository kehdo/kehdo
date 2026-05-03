package app.kehdo.core.network.api.dto

import kotlinx.serialization.Serializable

/**
 * DTOs for the conversation + reply + tone + usage endpoints. Mirrors
 * the schemas in `/contracts/openapi/kehdo.v1.yaml` exactly — field names
 * match the contract verbatim. Domain mapping happens in
 * `:data:conversation/mapper/`.
 */

// ---- /conversations -------------------------------------------------------

@Serializable
data class CreateConversationResponseDto(
    val conversationId: String,
    val uploadUrl: String,
    val uploadExpiresAt: String
)

@Serializable
data class ConversationDto(
    val id: String,
    val status: String,
    val messages: List<ParsedMessageDto>? = null,
    val createdAt: String
)

@Serializable
data class ParsedMessageDto(
    val speaker: String,
    val text: String,
    val confidence: Double? = null
)

@Serializable
data class ConversationPageDto(
    val items: List<ConversationDto>,
    val nextCursor: String? = null
)

// ---- /conversations/{id}/generate ----------------------------------------

@Serializable
data class GenerateRequestDto(
    val tone: String,
    val count: Int = 4
)

@Serializable
data class GenerateResponseDto(
    val conversationId: String,
    val tone: String,
    val replies: List<ReplyDto>,
    val generatedAt: String,
    val modelUsed: String? = null
)

@Serializable
data class ReplyDto(
    val id: String,
    val rank: Int,
    val text: String,
    val toneCode: String
)

// ---- /tones ---------------------------------------------------------------

@Serializable
data class ToneDto(
    val code: String,
    val name: String,
    val emoji: String,
    val description: String? = null,
    val isPro: Boolean
)

// ---- /me/usage ------------------------------------------------------------

@Serializable
data class UsageResponseDto(
    val dailyUsed: Int,
    val dailyLimit: Int,
    val resetAt: String
)
