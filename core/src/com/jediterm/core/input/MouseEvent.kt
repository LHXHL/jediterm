package com.jediterm.core.input

open class MouseEvent(val type: Type, val buttonCode: Int, val modifierKeys: Int) {
  enum class Type {
    PRESSED,
    RELEASED,
    MOVED,
    DRAGGED,
    WHEEL
  }
}
