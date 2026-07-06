package com.jediterm.terminal.emulator.mouse

data class MouseEventProcessingSettings(
  val isMouseReportingEnabled: Boolean,
  val isUsingAlternateBuffer: Boolean,
  val isSimulateMouseScrollWithArrowKeysInAlternateScreen: Boolean,
)
