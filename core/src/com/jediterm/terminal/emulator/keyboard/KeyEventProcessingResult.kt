package com.jediterm.terminal.emulator.keyboard

sealed interface KeyEventProcessingResult {
  val shouldScrollToBottom: Boolean

  data class StringResult(val string: String, override val shouldScrollToBottom: Boolean) : KeyEventProcessingResult
  data class BytesResult(val bytes: ByteArray, override val shouldScrollToBottom: Boolean) : KeyEventProcessingResult
  data object Unhandled : KeyEventProcessingResult {
    override val shouldScrollToBottom: Boolean = false
  }
}
