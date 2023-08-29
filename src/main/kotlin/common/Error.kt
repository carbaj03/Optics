package common

sealed interface Error

data class UnknownError(val message: String) : Error
data class AIError(val message: String) : Error