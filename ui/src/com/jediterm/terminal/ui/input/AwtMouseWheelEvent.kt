package com.jediterm.terminal.ui.input

import com.jediterm.core.input.MouseWheelEvent
import com.jediterm.terminal.emulator.mouse.MouseButtonCodes
import java.awt.event.MouseWheelEvent as AwtMouseWheelEventBase

class AwtMouseWheelEvent(
  private val awtMouseWheelEvent: AwtMouseWheelEventBase,
) : MouseWheelEvent(
  buttonCode = createButtonCode(awtMouseWheelEvent),
  modifierKeys = AwtMouseEvent.getModifierKeys(awtMouseWheelEvent),
  rotation = awtMouseWheelEvent.wheelRotation,
  unitsToScroll = awtMouseWheelEvent.unitsToScroll,
) {
  override fun toString(): String = awtMouseWheelEvent.toString()

  companion object {
    private fun createButtonCode(awtMouseWheelEvent: AwtMouseWheelEventBase): Int {
      return when {
        awtMouseWheelEvent.wheelRotation > 0 -> MouseButtonCodes.SCROLLUP
        awtMouseWheelEvent.wheelRotation < 0 -> MouseButtonCodes.SCROLLDOWN
        else -> MouseButtonCodes.NONE
      }
    }
  }
}
