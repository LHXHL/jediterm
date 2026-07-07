package com.jediterm.terminal.emulator.mouse

import com.jediterm.core.input.KeyEvent
import com.jediterm.core.input.MouseEvent
import com.jediterm.core.input.MouseWheelEvent
import com.jediterm.util.TestSession
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TerminalMouseEventEncoderTest {

  @Test
  fun `pressed events are encoded for each supported format`() {
    val event = MouseEvent(
      type = MouseEvent.Type.PRESSED,
      buttonCode = MouseButtonCodes.LEFT,
      modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG or MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG
    )

    assertArrayEquals(
      expectedXterm(button = 24, x = 0, y = 1),
      TerminalMouseEventEncoder().encode(
        event,
        0,
        1,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedXtermExt(button = 24, x = 94, y = 95),
      TerminalMouseEventEncoder().encode(
        event,
        94,
        95,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM_EXT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedUrxvt(button = 24, x = 4, y = 9),
      TerminalMouseEventEncoder().encode(
        event,
        4,
        9,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 24, x = 2, y = 3, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        2,
        3,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings()
      ),
    )
  }

  @Test
  fun `wheel events are encoded for each supported format when reporting is enabled for remote action`() {
    val event = MouseWheelEvent(
      buttonCode = MouseButtonCodes.SCROLLUP,
      modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG,
      unitsToScroll = 1,
    )

    assertArrayEquals(
      expectedXterm(button = 73, x = 6, y = 7),
      TerminalMouseEventEncoder().encode(
        event,
        6,
        7,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedXtermExt(button = 73, x = 94, y = 95),
      TerminalMouseEventEncoder().encode(
        event,
        94,
        95,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM_EXT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedUrxvt(button = 73, x = 8, y = 9),
      TerminalMouseEventEncoder().encode(
        event,
        8,
        9,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 73, x = 6, y = 7, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        6,
        7,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `released events are encoded for each supported format`() {
    val event = MouseEvent(
      type = MouseEvent.Type.RELEASED,
      buttonCode = MouseButtonCodes.RIGHT,
      modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG,
    )

    assertArrayEquals(
      expectedSgr(button = 10, x = 5, y = 6, release = true),
      TerminalMouseEventEncoder().encode(
        event,
        5,
        6,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedXterm(button = 11, x = 5, y = 6),
      TerminalMouseEventEncoder().encode(
        event,
        5,
        6,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedXtermExt(button = 11, x = 94, y = 95),
      TerminalMouseEventEncoder().encode(
        event,
        94,
        95,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_XTERM_EXT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedUrxvt(button = 11, x = 5, y = 6),
      TerminalMouseEventEncoder().encode(
        event,
        5,
        6,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `moved events are encoded for each supported format in all motion mode`() {
    val encoder = TerminalMouseEventEncoder()
    val event = MouseEvent(
      MouseEvent.Type.MOVED,
      MouseButtonCodes.NONE,
      0,
    )

    assertArrayEquals(
      expectedXterm(button = 35, x = 1, y = 2),
      encoder.encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedXtermExt(button = 35, x = 94, y = 95),
      encoder.encode(
        event,
        94,
        95,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM_EXT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedUrxvt(button = 35, x = 4, y = 6),
      encoder.encode(
        event,
        4,
        6,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 35, x = 3, y = 5, release = false),
      encoder.encode(
        event,
        3,
        5,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `dragged events are encoded for each supported format`() {
    val encoder = TerminalMouseEventEncoder()
    val dragEvent = MouseEvent(
      type = MouseEvent.Type.DRAGGED,
      buttonCode = MouseButtonCodes.MIDDLE,
      modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG,
    )

    assertArrayEquals(
      expectedXterm(button = 49, x = 2, y = 3),
      encoder.encode(
        dragEvent,
        2,
        3,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedXtermExt(button = 49, x = 94, y = 95),
      encoder.encode(
        dragEvent,
        94,
        95,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM_EXT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedUrxvt(button = 49, x = 8, y = 9),
      encoder.encode(
        dragEvent,
        8,
        9,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 49, x = 5, y = 7, release = false),
      encoder.encode(
        dragEvent,
        5,
        7,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `pressed event is encoded or not according to mouse mode`() {
    val event = MouseEvent(
      MouseEvent.Type.PRESSED,
      MouseButtonCodes.RIGHT,
      0,
    )

    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_NONE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 2, x = 1, y = 2, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 2, x = 1, y = 2, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 2, x = 1, y = 2, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_HILITE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        1,
        2,
        MouseMode.MOUSE_REPORTING_FOCUS,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `wheel event is encoded or not according to mouse mode when reporting is enabled for remote action`() {
    val event = MouseWheelEvent(
      buttonCode = MouseButtonCodes.SCROLLDOWN,
      modifierKeys = 0,
      unitsToScroll = 1,
    )

    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_NONE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 64, x = 2, y = 4, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 64, x = 2, y = 4, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 64, x = 2, y = 4, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_HILITE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        2,
        4,
        MouseMode.MOUSE_REPORTING_FOCUS,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `released event is encoded or not according to mouse mode`() {
    val event = MouseEvent(MouseEvent.Type.RELEASED, MouseButtonCodes.LEFT, 0)

    assertNull(TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_NONE, MouseFormat.MOUSE_FORMAT_SGR,
                                                  TestSession(80, 24).terminal,
                                                  mouseEventProcessingSettings()
    ))
    assertArrayEquals(
      expectedSgr(button = 0, x = 5, y = 6, release = true),
      TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_NORMAL, MouseFormat.MOUSE_FORMAT_SGR,
                                         TestSession(80, 24).terminal,
                                         mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 0, x = 5, y = 6, release = true),
      TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_BUTTON_MOTION, MouseFormat.MOUSE_FORMAT_SGR,
                                         TestSession(80, 24).terminal,
                                         mouseEventProcessingSettings()
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 0, x = 5, y = 6, release = true),
      TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_ALL_MOTION, MouseFormat.MOUSE_FORMAT_SGR,
                                         TestSession(80, 24).terminal,
                                         mouseEventProcessingSettings()
      ),
    )
    assertNull(TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_HILITE, MouseFormat.MOUSE_FORMAT_SGR,
                                                  TestSession(80, 24).terminal,
                                                  mouseEventProcessingSettings()
    ))
    assertNull(TerminalMouseEventEncoder().encode(event, 5, 6, MouseMode.MOUSE_REPORTING_FOCUS, MouseFormat.MOUSE_FORMAT_SGR,
                                                  TestSession(80, 24).terminal,
                                                  mouseEventProcessingSettings()
    ))
  }

  @Test
  fun `moved event is encoded or not according to mouse mode`() {
    val event = MouseEvent(MouseEvent.Type.MOVED, MouseButtonCodes.NONE, 0)

    assertNull(TerminalMouseEventEncoder().encode(
      event,
      1,
      1,
      MouseMode.MOUSE_REPORTING_NONE,
      MouseFormat.MOUSE_FORMAT_SGR,
      TestSession(80, 24).terminal,
      mouseEventProcessingSettings()
    ))
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        2,
        1,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        3,
        1,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 35, x = 4, y = 1, release = false),
      TerminalMouseEventEncoder().encode(
        event,
        4,
        1,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        5,
        1,
        MouseMode.MOUSE_REPORTING_HILITE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        event,
        6,
        1,
        MouseMode.MOUSE_REPORTING_FOCUS,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `dragged event is encoded or not according to mouse mode`() {
    val dragEvent = MouseEvent(
      type = MouseEvent.Type.DRAGGED,
      buttonCode = MouseButtonCodes.MIDDLE,
      modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG,
    )

    assertNull(
      TerminalMouseEventEncoder().encode(
        dragEvent,
        1,
        3,
        MouseMode.MOUSE_REPORTING_NONE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        dragEvent,
        2,
        3,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 49, x = 3, y = 3, release = false),
      TerminalMouseEventEncoder().encode(
        dragEvent,
        3,
        3,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 49, x = 4, y = 3, release = false),
      TerminalMouseEventEncoder().encode(
        dragEvent,
        4,
        3,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        dragEvent,
        5,
        3,
        MouseMode.MOUSE_REPORTING_HILITE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        dragEvent,
        6,
        3,
        MouseMode.MOUSE_REPORTING_FOCUS,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `mouse reporting policy suppresses non wheel events when reporting is disabled or action is local`() {
    val encoder = TerminalMouseEventEncoder()
    val acceptedSettings = mouseEventProcessingSettings()

    val cases = listOf(
      Triple(
        MouseEvent(MouseEvent.Type.PRESSED, MouseButtonCodes.LEFT, 0),
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
      ),
      Triple(
        MouseEvent(MouseEvent.Type.RELEASED, MouseButtonCodes.LEFT, 0),
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
      ),
      Triple(
        MouseEvent(MouseEvent.Type.MOVED, MouseButtonCodes.NONE, 0),
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
      ),
      Triple(
        MouseEvent(MouseEvent.Type.DRAGGED, MouseButtonCodes.LEFT, 0),
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
      ),
    )

    for ((event, mode, format) in cases) {
      assertNull(
        encoder.encode(
          event,
          3,
          4,
          mode,
          format,
          TestSession(80, 24).terminal,
          acceptedSettings.copy(
            isMouseReportingEnabled = false,
          ),
        ),
      )
      assertNull(
        encoder.encode(
          MouseEvent(event.type, event.buttonCode, event.modifierKeys or MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG),
          3,
          4,
          mode,
          format,
          TestSession(80, 24).terminal,
          acceptedSettings,
        ),
      )
    }
  }

  @Test
  fun `wheel event simulates arrow keys in alternate buffer when reporting policy is not accepted`() {
    val terminal = TestSession(80, 24).terminal
    val settings = mouseEventProcessingSettings(
      mouseReportingEnabled = false,
      usingAlternateBuffer = true,
      simulateMouseScrollWithArrowKeysInAlternateScreen = true,
    )

    assertArrayEquals(
      repeatBytes(terminal.getCodeForKey(KeyEvent.VK_UP, 0), 3),
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLUP,
          modifierKeys = 0,
          unitsToScroll = 3,
        ),
        5,
        6,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        terminal,
        settings,
      ),
    )
    assertArrayEquals(
      repeatBytes(terminal.getCodeForKey(KeyEvent.VK_DOWN, 0), 2),
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLDOWN,
          modifierKeys = 0,
          unitsToScroll = 2,
        ),
        5,
        6,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        terminal,
        settings,
      ),
    )
  }

  @Test
  fun `wheel event returns null when reporting policy is not accepted and arrow simulation does not apply`() {
    val terminal = TestSession(80, 24).terminal

    assertNull(
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLUP,
          modifierKeys = 0,
          unitsToScroll = 1,
        ),
        1,
        2,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        terminal,
        mouseEventProcessingSettings(mouseReportingEnabled = false),
      ),
    )
    assertNull(
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLUP,
          modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG,
          unitsToScroll = 1,
        ),
        1,
        2,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        terminal,
        mouseEventProcessingSettings(
          usingAlternateBuffer = true,
          simulateMouseScrollWithArrowKeysInAlternateScreen = true,
        ),
      ),
    )
  }

  @Test
  fun `wheel event prefers mouse report over arrow simulation when reporting policy is accepted`() {
    val terminal = TestSession(80, 24).terminal

    assertArrayEquals(
      expectedSgr(button = 65, x = 2, y = 3, release = false),
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLUP,
          modifierKeys = 0,
          unitsToScroll = 2,
        ),
        2,
        3,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        terminal,
        mouseEventProcessingSettings(
          mouseReportingEnabled = true,
          usingAlternateBuffer = true,
          simulateMouseScrollWithArrowKeysInAlternateScreen = true,
        ),
      ),
    )
  }

  @Test
  fun `dragged events require a button`() {
    val encoder = TerminalMouseEventEncoder()

    assertNull(
      encoder.encode(
        MouseEvent(
          MouseEvent.Type.DRAGGED,
          MouseButtonCodes.NONE,
          0,
        ),
        8,
        9,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_URXVT,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `pressed event applies modifier flags`() {
    assertArrayEquals(
      expectedSgr(button = MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG, x = 1, y = 1, release = false),
      TerminalMouseEventEncoder().encode(
        MouseEvent(
          MouseEvent.Type.PRESSED,
          MouseButtonCodes.LEFT,
          MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG,
        ),
        1,
        1,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG, x = 1, y = 1, release = false),
      TerminalMouseEventEncoder().encode(
        MouseEvent(
          MouseEvent.Type.PRESSED,
          MouseButtonCodes.LEFT,
          MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG,
        ),
        1,
        1,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `shifted pressed event is suppressed as local mouse action`() {
    assertNull(
      TerminalMouseEventEncoder().encode(
        MouseEvent(
          MouseEvent.Type.PRESSED,
          MouseButtonCodes.LEFT,
          MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG,
        ),
        1,
        1,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `event specific flags are combined with modifier flags`() {
    assertArrayEquals(
      expectedSgr(button = 73, x = 2, y = 2, release = false),
      TerminalMouseEventEncoder().encode(
        MouseWheelEvent(
          buttonCode = MouseButtonCodes.SCROLLUP,
          modifierKeys = MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG,
          unitsToScroll = 1,
        ),
        2,
        2,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedSgr(button = 49, x = 2, y = 3, release = false),
      TerminalMouseEventEncoder().encode(
        MouseEvent(
          MouseEvent.Type.DRAGGED,
          MouseButtonCodes.MIDDLE,
          MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG,
        ),
        2,
        3,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `moved events deduplicate repeated coordinates`() {
    val encoder = TerminalMouseEventEncoder()
    val event = MouseEvent(
      MouseEvent.Type.MOVED,
      MouseButtonCodes.NONE,
      0,
    )

    assertArrayEquals(
      expectedXterm(button = 35, x = 10, y = 11),
      encoder.encode(
        event,
        10,
        11,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      encoder.encode(
        event,
        10,
        11,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertArrayEquals(
      expectedXterm(button = 35, x = 11, y = 11),
      encoder.encode(
        event,
        11,
        11,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_XTERM,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `suppressed move still updates last point until release resets it`() {
    val encoder = TerminalMouseEventEncoder()
    val moveEvent = MouseEvent(
      MouseEvent.Type.MOVED,
      MouseButtonCodes.NONE,
      0,
    )

    assertNull(
      encoder.encode(
        moveEvent,
        7,
        8,
        MouseMode.MOUSE_REPORTING_NORMAL,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      encoder.encode(
        moveEvent,
        7,
        8,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )

    assertNull(
      encoder.encode(
        MouseEvent(MouseEvent.Type.RELEASED, MouseButtonCodes.NONE, 0),
        7,
        8,
        MouseMode.MOUSE_REPORTING_NONE,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )

    assertArrayEquals(
      expectedSgr(button = 35, x = 7, y = 8, release = false),
      encoder.encode(
        moveEvent,
        7,
        8,
        MouseMode.MOUSE_REPORTING_ALL_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
  }

  @Test
  fun `drag deduplication is cleared by release`() {
    val encoder = TerminalMouseEventEncoder()
    val dragEvent = MouseEvent(
      MouseEvent.Type.DRAGGED,
      MouseButtonCodes.LEFT,
      0,
    )

    assertArrayEquals(
      expectedSgr(button = 32, x = 1, y = 1, release = false),
      encoder.encode(
        dragEvent,
        1,
        1,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )
    assertNull(
      encoder.encode(
        dragEvent,
        1,
        1,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )

    assertArrayEquals(
      expectedSgr(button = 0, x = 1, y = 1, release = true),
      encoder.encode(
        MouseEvent(MouseEvent.Type.RELEASED, MouseButtonCodes.LEFT, 0),
        1,
        1,
        MouseMode.MOUSE_REPORTING_BUTTON_MOTION,
        MouseFormat.MOUSE_FORMAT_SGR,
        TestSession(80, 24).terminal,
        mouseEventProcessingSettings(),
      ),
    )

    assertArrayEquals(
      expectedSgr(button = 32, x = 1, y = 1, release = false),
      encoder.encode(dragEvent, 1, 1, MouseMode.MOUSE_REPORTING_BUTTON_MOTION, MouseFormat.MOUSE_FORMAT_SGR, TestSession(80, 24).terminal,
                     mouseEventProcessingSettings()
      ),
    )
  }

  private fun mouseEventProcessingSettings(
    mouseReportingEnabled: Boolean = true,
    usingAlternateBuffer: Boolean = false,
    simulateMouseScrollWithArrowKeysInAlternateScreen: Boolean = false,
  ): MouseEventProcessingSettings {
    return MouseEventProcessingSettings(
      isMouseReportingEnabled = mouseReportingEnabled,
      isUsingAlternateBuffer = usingAlternateBuffer,
      isSimulateMouseScrollWithArrowKeysInAlternateScreen = simulateMouseScrollWithArrowKeysInAlternateScreen,
    )
  }

  private fun repeatBytes(bytes: ByteArray, repeatCount: Int): ByteArray {
    val result = ByteArray(bytes.size * repeatCount)
    repeat(repeatCount) { index ->
      System.arraycopy(bytes, 0, result, index * bytes.size, bytes.size)
    }
    return result
  }

  private fun expectedXterm(button: Int, x: Int, y: Int): ByteArray {
    return ("\u001B[M" + (32 + button).toChar() + (33 + x).toChar() + (33 + y).toChar()).toByteArray(Charsets.ISO_8859_1)
  }

  private fun expectedXtermExt(button: Int, x: Int, y: Int): ByteArray {
    return ("\u001B[M" + (32 + button).toChar() + (33 + x).toChar() + (33 + y).toChar()).toByteArray(Charsets.UTF_8)
  }

  private fun expectedUrxvt(button: Int, x: Int, y: Int): ByteArray {
    return "\u001B[${32 + button};${x + 1};${y + 1}M".toByteArray(Charsets.UTF_8)
  }

  private fun expectedSgr(button: Int, x: Int, y: Int, release: Boolean): ByteArray {
    val suffix = if (release) "m" else "M"
    return "\u001B[<$button;${x + 1};${y + 1}$suffix".toByteArray(Charsets.UTF_8)
  }
}
