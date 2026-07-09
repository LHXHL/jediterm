package com.jediterm.terminal.emulator.keyboard

import com.jediterm.core.input.KeyInputEvent
import com.jediterm.terminal.Terminal
import com.jediterm.core.input.InputEvent
import com.jediterm.core.input.KeyEvent

object TerminalKeyEventProcessor {
  private const val ASCII_NUL: Byte = 0
  private const val ASCII_ESC: Byte = 27

  @JvmStatic
  fun processKey(
    event: KeyInputEvent,
    terminal: Terminal,
    settings: KeyEventProcessingSettings,
  ): KeyEventProcessingResult {
    return when (event.type) {
      KeyInputEvent.Type.PRESSED -> processTerminalKeyPressed(
        event,
        terminal,
        settings,
      )

      KeyInputEvent.Type.TYPED -> processTerminalKeyTyped(
        event,
        settings,
      )

      else -> KeyEventProcessingResult.Unhandled
    }
  }

  private fun processTerminalKeyPressed(
    event: KeyInputEvent,
    terminal: Terminal,
    settings: KeyEventProcessingSettings,
  ): KeyEventProcessingResult {
    val keyCode = event.keyCode
    val keyChar = event.keyChar

    // numLock does not change the code sent by keypad VK_DELETE
    // although it send the char '.'
    if (keyCode == KeyEvent.VK_DELETE && keyChar == '.') {
      return KeyEventProcessingResult.BytesResult(byteArrayOf('.'.code.toByte()), false)
    }
    // CTRL + Space is not handled in KeyEvent; handle it manually
    if (keyChar == ' ' && (event.modifiersEx and InputEvent.CTRL_DOWN_MASK) != 0) {
      return KeyEventProcessingResult.BytesResult(byteArrayOf(ASCII_NUL), false)
    }

    // Shift+Enter handling as Esc+CR.
    if (settings.shiftEnterSendsEscCR && keyCode == KeyEvent.VK_ENTER && isShiftPressedOnly(event)) {
      return KeyEventProcessingResult.BytesResult(byteArrayOf(ASCII_ESC, '\r'.code.toByte()), false)
    }

    val code = terminal.getCodeForKey(keyCode, event.modifiers)
    if (code != null) {
      val shouldScrollToBottom = settings.scrollToBottomOnTyping && isCodeThatScrolls(keyCode)
      return KeyEventProcessingResult.BytesResult(code, shouldScrollToBottom)
    }
    if (isAltPressedOnly(event) && Character.isDefined(keyChar) && settings.altSendsEscape) {
      // Cannot use event.keyChar on macOS:
      //  Option+f produces event.keyChar='ƒ' (402), but 'f' (102) is needed.
      //  Option+b produces event.keyChar='∫' (8747), but 'b' (98) is needed.
      return KeyEventProcessingResult.StringResult(
        String(
          charArrayOf(
            Char(ASCII_ESC.toUShort()),
            simpleMapKeyCodeToChar(event),
          ),
        ),
        false,
      )
    }
    if (Character.isISOControl(keyChar)) { // keys filtered out here will be processed in processTerminalKeyTyped
      return processCharacter(event, settings)
    }
    return KeyEventProcessingResult.Unhandled
  }

  private fun processTerminalKeyTyped(
    event: KeyInputEvent,
    settings: KeyEventProcessingSettings,
  ): KeyEventProcessingResult {
    if (!Character.isISOControl(event.keyChar)) { // keys filtered out here will be processed in processTerminalKeyPressed
      return processCharacter(event, settings)
    }
    return KeyEventProcessingResult.Unhandled
  }

  private fun processCharacter(
    event: KeyInputEvent,
    settings: KeyEventProcessingSettings,
  ): KeyEventProcessingResult {
    if (isAltPressedOnly(event) && settings.altSendsEscape) {
      return KeyEventProcessingResult.Unhandled
    }
    val keyChar = event.keyChar

    if (keyChar == '`' && (event.modifiersEx and InputEvent.META_DOWN_MASK) != 0) {
      // Command + backtick is a short-cut on Mac OSX, so we shouldn't type anything
      return KeyEventProcessingResult.Unhandled
    }

    return KeyEventProcessingResult.StringResult(
      keyChar.toString(),
      settings.scrollToBottomOnTyping,
    )
  }

  private fun isAltPressedOnly(event: KeyInputEvent): Boolean {
    val modifiersEx = event.modifiersEx
    return (modifiersEx and InputEvent.ALT_DOWN_MASK) != 0
      && (modifiersEx and InputEvent.ALT_GRAPH_DOWN_MASK) == 0
      && (modifiersEx and InputEvent.CTRL_DOWN_MASK) == 0
      && (modifiersEx and InputEvent.SHIFT_DOWN_MASK) == 0
  }

  private fun isShiftPressedOnly(event: KeyInputEvent): Boolean {
    val modifiersEx = event.modifiersEx
    return (modifiersEx and InputEvent.SHIFT_DOWN_MASK) != 0
      && (modifiersEx and InputEvent.ALT_DOWN_MASK) == 0
      && (modifiersEx and InputEvent.ALT_GRAPH_DOWN_MASK) == 0
      && (modifiersEx and InputEvent.CTRL_DOWN_MASK) == 0
  }

  private fun isCodeThatScrolls(keyCode: Int): Boolean {
    return keyCode == KeyEvent.VK_UP
      || keyCode == KeyEvent.VK_DOWN
      || keyCode == KeyEvent.VK_LEFT
      || keyCode == KeyEvent.VK_RIGHT
      || keyCode == KeyEvent.VK_BACK_SPACE
      || keyCode == KeyEvent.VK_INSERT
      || keyCode == KeyEvent.VK_DELETE
      || keyCode == KeyEvent.VK_ENTER
      || keyCode == KeyEvent.VK_HOME
      || keyCode == KeyEvent.VK_END
      || keyCode == KeyEvent.VK_PAGE_UP
      || keyCode == KeyEvent.VK_PAGE_DOWN
  }

  private fun simpleMapKeyCodeToChar(event: KeyInputEvent): Char {
    // zsh requires proper case of letter
    if ((event.modifiersEx and InputEvent.SHIFT_DOWN_MASK) != 0) {
      return event.keyCode.toChar().uppercaseChar()
    }
    return event.keyCode.toChar().lowercaseChar()
  }
}
