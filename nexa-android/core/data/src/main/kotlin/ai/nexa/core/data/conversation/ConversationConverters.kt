package ai.nexa.core.data.conversation

import androidx.room.TypeConverter

object ConversationConverters {
    @TypeConverter
    fun messageRoleToString(role: MessageRole): String = role.name

    @TypeConverter
    fun stringToMessageRole(value: String): MessageRole = MessageRole.valueOf(value)
}
