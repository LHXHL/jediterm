package com.jediterm.terminal.ui.input

import com.jediterm.core.input.MouseEvent
import com.jediterm.terminal.emulator.mouse.MouseButtonCodes
import com.jediterm.terminal.emulator.mouse.MouseButtonModifierFlags
import java.awt.event.MouseEvent as AwtMouseEventBase
import javax.swing.SwingUtilities

class AwtMouseEvent(private val awtMouseEvent: AwtMouseEventBase) : MouseEvent(
  getType(awtMouseEvent),
  createButtonCode(awtMouseEvent),
  getModifierKeys(awtMouseEvent),
) {
  override fun toString(): String = awtMouseEvent.toString()

  companion object {
    private fun createButtonCode(awtMouseEvent: AwtMouseEventBase): Int {
      // For mouse dragged, button is stored in modifiers.
      return when {
        SwingUtilities.isLeftMouseButton(awtMouseEvent) -> MouseButtonCodes.LEFT
        SwingUtilities.isMiddleMouseButton(awtMouseEvent) -> MouseButtonCodes.MIDDLE
        SwingUtilities.isRightMouseButton(awtMouseEvent) -> MouseButtonCodes.NONE
        else -> MouseButtonCodes.NONE
      }
    }

    internal fun getModifierKeys(awtMouseEvent: AwtMouseEventBase): Int {
      var modifier = 0
      if (awtMouseEvent.isControlDown) {
        modifier = modifier or MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG
      }
      if (awtMouseEvent.isShiftDown) {
        modifier = modifier or MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG
      }
      if (awtMouseEvent.isMetaDown) {
        modifier = modifier or MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG
      }
      return modifier
    }

    private fun getType(awtMouseEvent: AwtMouseEventBase): Type {
      return when (awtMouseEvent.id) {
        AwtMouseEventBase.MOUSE_PRESSED -> Type.PRESSED
        AwtMouseEventBase.MOUSE_RELEASED -> Type.RELEASED
        AwtMouseEventBase.MOUSE_MOVED -> Type.MOVED
        AwtMouseEventBase.MOUSE_DRAGGED -> Type.DRAGGED
        else -> throw IllegalArgumentException("Unsupported AWT mouse event id: ${awtMouseEvent.id}")
      }
    }
  }
}
