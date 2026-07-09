package com.jediterm.terminal.emulator.keyboard

import com.jediterm.core.input.InputEvent as LegacyInputEvent
import com.jediterm.core.input.KeyInputEvent
import com.jediterm.core.util.Ascii
import com.jediterm.util.TestSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

class TerminalKeyEventProcessorTest {

  private val noModifiers = Modifiers(0, 0)
  private val shift = Modifiers(LegacyInputEvent.SHIFT_MASK, InputEvent.SHIFT_DOWN_MASK)
  private val ctrl = Modifiers(LegacyInputEvent.CTRL_MASK, InputEvent.CTRL_DOWN_MASK)
  private val alt = Modifiers(LegacyInputEvent.ALT_MASK, InputEvent.ALT_DOWN_MASK)
  private val meta = Modifiers(LegacyInputEvent.META_MASK, InputEvent.META_DOWN_MASK)
  private val ctrlShift = Modifiers(
    LegacyInputEvent.CTRL_MASK or LegacyInputEvent.SHIFT_MASK,
    InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK,
  )

  @Test
  fun `pressed delete with dot returns dot byte`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_DELETE, '.',
        noModifiers.legacy,
        noModifiers.extended
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(byteArrayOf('.'.code.toByte()), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed ctrl space returns nul byte`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_SPACE,
        ' ',
        ctrl.legacy,
        ctrl.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(byteArrayOf(0), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed shift enter returns escape carriage return when enabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_ENTER,
        '\n',
        shift.legacy,
        shift.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(true, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(byteArrayOf(27, '\r'.code.toByte()), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed shift enter uses terminal enter mapping when disabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_ENTER,
        '\n',
        shift.legacy,
        shift.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(byteArrayOf('\r'.code.toByte()), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed uses terminal key code result`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_F1,
        KeyEvent.CHAR_UNDEFINED,
        ctrl.legacy,
        ctrl.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(prependEsc("[1;5P"), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed scrolling key sets scroll flag when enabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.PRESSED, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, true, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(prependEsc("[D"), result.bytes)
    assertTrue(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed scrolling key does not set scroll flag when disabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.PRESSED, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(prependEsc("[D"), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed non scrolling key does not set scroll flag`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.PRESSED, KeyEvent.VK_F1, KeyEvent.CHAR_UNDEFINED, noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, true, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(prependEsc("OP"), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed alt only returns escape prefixed character`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_F,
        'ƒ',
        alt.legacy,
        alt.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, true),
    )

    assertTrue(result is KeyEventProcessingResult.StringResult)
    result as KeyEventProcessingResult.StringResult
    assertEquals("\u001bf", result.string)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `pressed alt only is unhandled when alt sends escape disabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_F,
        'ƒ',
        alt.legacy,
        alt.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertSame(KeyEventProcessingResult.Unhandled, result)
  }

  @Test
  fun `pressed printable character without terminal mapping is unhandled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.PRESSED, KeyEvent.VK_A, 'a', noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertSame(KeyEventProcessingResult.Unhandled, result)
  }

  @Test
  fun `typed printable character returns string result`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.TYPED, KeyEvent.VK_UNDEFINED, 'a', noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.StringResult)
    result as KeyEventProcessingResult.StringResult
    assertEquals("a", result.string)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `typed printable character uses scroll setting`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.TYPED, KeyEvent.VK_UNDEFINED, 'a', noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, true, false),
    )

    assertTrue(result is KeyEventProcessingResult.StringResult)
    result as KeyEventProcessingResult.StringResult
    assertEquals("a", result.string)
    assertTrue(result.shouldScrollToBottom)
  }

  @Test
  fun `typed alt only returns character when alt sends escape disabled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.TYPED,
        KeyEvent.VK_A,
        'a',
        alt.legacy,
        alt.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.StringResult)
    result as KeyEventProcessingResult.StringResult
    assertEquals("a", result.string)
    assertFalse(result.shouldScrollToBottom)
  }

  @Test
  fun `typed control character is unhandled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(KeyInputEvent.Type.TYPED, KeyEvent.VK_UNDEFINED, '\n', noModifiers.legacy, noModifiers.extended),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertSame(KeyEventProcessingResult.Unhandled, result)
  }

  @Test
  fun `typed alt only is unhandled when alt sends escape`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.TYPED,
        KeyEvent.VK_A,
        'a',
        alt.legacy,
        alt.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, true),
    )

    assertSame(KeyEventProcessingResult.Unhandled, result)
  }

  @Test
  fun `typed meta backtick is unhandled`() {
    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.TYPED,
        KeyEvent.VK_BACK_QUOTE,
        '`',
        meta.legacy,
        meta.extended,
      ),
      TestSession(80, 24).terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertSame(KeyEventProcessingResult.Unhandled, result)
  }

  @Test
  fun `pressed uses modifier-aware terminal mapping`() {
    val terminal = TestSession(80, 24).terminal

    val result = TerminalKeyEventProcessor.processKey(
      KeyInputEvent(
        KeyInputEvent.Type.PRESSED,
        KeyEvent.VK_F2,
        KeyEvent.CHAR_UNDEFINED,
        ctrlShift.legacy,
        ctrlShift.extended,
      ),
      terminal,
      KeyEventProcessingSettings(false, false, false),
    )

    assertTrue(result is KeyEventProcessingResult.BytesResult)
    result as KeyEventProcessingResult.BytesResult
    assertArrayEquals(prependEsc("[1;6Q"), result.bytes)
    assertFalse(result.shouldScrollToBottom)
  }

  private data class Modifiers(val legacy: Int, val extended: Int)

  private fun prependEsc(str: String): ByteArray = (Ascii.ESC_CHAR + str).toByteArray(Charsets.UTF_8)
}
