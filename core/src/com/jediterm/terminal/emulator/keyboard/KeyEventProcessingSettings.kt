package com.jediterm.terminal.emulator.keyboard

data class KeyEventProcessingSettings(
  val shiftEnterSendsEscCR: Boolean,
  val scrollToBottomOnTyping: Boolean,
  val altSendsEscape: Boolean,
)
