package com.jediterm.core.input

open class MouseWheelEvent(
  buttonCode: Int,
  modifierKeys: Int,
  val unitsToScroll: Int,
) : MouseEvent(Type.WHEEL, buttonCode, modifierKeys)
