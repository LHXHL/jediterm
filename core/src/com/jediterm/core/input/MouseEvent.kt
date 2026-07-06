package com.jediterm.core.input

import com.jediterm.terminal.emulator.mouse.MouseButtonModifierFlags

open class MouseEvent(val type: Type, val buttonCode: Int, val modifierKeys: Int) {
  fun isShiftDown(): Boolean = (modifierKeys and MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG) != 0

  enum class Type {
    PRESSED,
    RELEASED,
    MOVED,
    DRAGGED,
    WHEEL
  }
}
