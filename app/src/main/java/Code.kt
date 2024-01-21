// Utility class or any other suitable location
object CodeGenerator {
    fun generateSpecialCode(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
}
