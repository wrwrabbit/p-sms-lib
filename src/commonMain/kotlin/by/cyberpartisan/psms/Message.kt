package by.cyberpartisan.psms

public data class Message(
    public val text: String,
    public val channelId: Int? = null,
    public val isLegacy: Boolean = false
)
