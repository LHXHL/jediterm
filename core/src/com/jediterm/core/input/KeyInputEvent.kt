package com.jediterm.core.input

data class KeyInputEvent(
  val type: Type,
  val keyCode: Int,
  val keyChar: Char,
  val modifiers: Int,
  val modifiersEx: Int,
) {
  enum class Type {
    PRESSED,
    TYPED,
  }
}
