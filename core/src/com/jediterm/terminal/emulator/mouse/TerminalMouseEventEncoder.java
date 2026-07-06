package com.jediterm.terminal.emulator.mouse;

import com.jediterm.core.compatibility.Point;
import com.jediterm.core.input.KeyEvent;
import com.jediterm.core.input.MouseEvent;
import com.jediterm.core.input.MouseWheelEvent;
import com.jediterm.terminal.Terminal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public final class TerminalMouseEventEncoder {
  private @Nullable Point myLastMotionReport = null;
  private static final Logger LOG = LoggerFactory.getLogger(TerminalMouseEventEncoder.class);

  /**
   * @param x zero-based column in the terminal cell grid
   * @param y zero-based row in the terminal cell grid
   */
  public byte @Nullable [] encode(@NotNull MouseEvent event,
                                  int x,
                                  int y,
                                  @NotNull MouseMode mouseMode,
                                  @NotNull MouseFormat mouseFormat,
                                  Terminal terminal,
                                  MouseEventProcessingSettings settings) {
    switch (event.getType()) {
      case PRESSED:
        if (settings.isMouseReportingEnabled() && isRemoteMouseAction(event, mouseMode)) {
          return encodePressed(event, x, y, mouseMode, mouseFormat);
        }
        return null;
      case RELEASED:
        if (settings.isMouseReportingEnabled() && isRemoteMouseAction(event, mouseMode)) {
          return encodeReleased(event, x, y, mouseMode, mouseFormat);
        }
        myLastMotionReport = null;
        return null;
      case MOVED:
        if (settings.isMouseReportingEnabled() && isRemoteMouseAction(event, mouseMode)) {
          return encodeMoved(x, y, mouseMode, mouseFormat);
        }
        myLastMotionReport = new Point(x, y);
        return null;
      case DRAGGED:
        if (settings.isMouseReportingEnabled() && isRemoteMouseAction(event, mouseMode)) {
          return encodeDragged(event, x, y, mouseMode, mouseFormat);
        }
        myLastMotionReport = new Point(x, y);
        return null;
      case WHEEL:
        if (settings.isMouseReportingEnabled() && isRemoteMouseAction(event, mouseMode)) {
          return encodePressed(event, x, y, mouseMode, mouseFormat);
        }
        return encodeWheel((MouseWheelEvent) event, terminal, settings);
      default:
        return null;
    }
  }

  private byte @Nullable [] encodePressed(@NotNull MouseEvent event,
                                          int x,
                                          int y,
                                          @NotNull MouseMode mouseMode,
                                          @NotNull MouseFormat mouseFormat) {

    if (!shouldSendMouseData(mouseMode, MouseMode.MOUSE_REPORTING_NORMAL, MouseMode.MOUSE_REPORTING_BUTTON_MOTION)) {
      return null;
    }

    int cb = event.getButtonCode();
    if (cb == MouseButtonCodes.NONE) {
      return null;
    }

    if (cb == MouseButtonCodes.SCROLLDOWN || cb == MouseButtonCodes.SCROLLUP) {
      // convert x11 scroll button number to terminal button code
      int offset = MouseButtonCodes.SCROLLDOWN;
      cb -= offset;
      cb |= MouseButtonModifierFlags.MOUSE_BUTTON_SCROLL_FLAG;
    }

    cb |= event.getModifierKeys();
    return encode(cb, x + 1, y + 1, mouseFormat);
  }

  private byte @Nullable [] encodeReleased(@NotNull MouseEvent event,
                                           int x,
                                           int y,
                                           @NotNull MouseMode mouseMode,
                                           @NotNull MouseFormat mouseFormat) {
    try {
      if (!shouldSendMouseData(mouseMode, MouseMode.MOUSE_REPORTING_NORMAL, MouseMode.MOUSE_REPORTING_BUTTON_MOTION)) {
        return null;
      }

      int cb = event.getButtonCode();
      if (cb == MouseButtonCodes.NONE) {
        return null;
      }

      if (mouseFormat == MouseFormat.MOUSE_FORMAT_SGR) {
        // for SGR 1006 mode
        cb |= MouseButtonModifierFlags.MOUSE_BUTTON_SGR_RELEASE_FLAG;
      }
      else {
        // for 1000/1005/1015 mode
        cb = MouseButtonCodes.RELEASE;
      }

      cb |= event.getModifierKeys();
      return encode(cb, x + 1, y + 1, mouseFormat);
    }
    finally {
      myLastMotionReport = null;
    }
  }

  private byte @Nullable [] encodeMoved(int x,
                                        int y,
                                        @NotNull MouseMode mouseMode,
                                        @NotNull MouseFormat mouseFormat) {
    Point point = new Point(x, y);
    if (myLastMotionReport != null && myLastMotionReport.equals(point)) {
      return null;
    }
    try {
      if (!shouldSendMouseData(mouseMode, MouseMode.MOUSE_REPORTING_ALL_MOTION)) {
        return null;
      }
      return encode(
        MouseButtonCodes.RELEASE | MouseButtonModifierFlags.MOUSE_BUTTON_MOTION_FLAG,
        x + 1,
        y + 1,
        mouseFormat
      );
    }
    finally {
      myLastMotionReport = point;
    }
  }

  private byte @Nullable [] encodeDragged(@NotNull MouseEvent event,
                                          int x,
                                          int y,
                                          @NotNull MouseMode mouseMode,
                                          @NotNull MouseFormat mouseFormat) {
    Point point = new Point(x, y);
    if (myLastMotionReport != null && myLastMotionReport.equals(point)) {
      return null;
    }
    try {
      if (!shouldSendMouseData(mouseMode, MouseMode.MOUSE_REPORTING_BUTTON_MOTION)) {
        return null;
      }

      // when dragging, button is not in "button", but in "modifier"
      int cb = event.getButtonCode();
      if (cb == MouseButtonCodes.NONE) {
        return null;
      }
      cb |= MouseButtonModifierFlags.MOUSE_BUTTON_MOTION_FLAG;
      cb |= event.getModifierKeys();
      return encode(cb, x + 1, y + 1, mouseFormat);
    }
    finally {
      myLastMotionReport = point;
    }
  }

  private boolean shouldSendMouseData(@NotNull MouseMode mouseMode, @NotNull MouseMode... eligibleModes) {
    if (mouseMode == MouseMode.MOUSE_REPORTING_NONE) {
      return false;
    }
    if (mouseMode == MouseMode.MOUSE_REPORTING_ALL_MOTION) {
      return true;
    }
    for (MouseMode mode : eligibleModes) {
      if (mouseMode == mode) {
        return true;
      }
    }
    return false;
  }

  private byte @NotNull [] encode(int button, int x, int y, @NotNull MouseFormat mouseFormat) {
    StringBuilder sb = new StringBuilder();
    String charset = "UTF-8"; // extended mode requires UTF-8 encoding
    switch (mouseFormat) {
      case MOUSE_FORMAT_XTERM_EXT:
        sb.append(String.format("\033[M%c%c%c",
                                (char) (32 + button),
                                (char) (32 + x),
                                (char) (32 + y)));
        break;
      case MOUSE_FORMAT_URXVT:
        sb.append(String.format("\033[%d;%d;%dM", 32 + button, x, y));
        break;
      case MOUSE_FORMAT_SGR:
        if ((button & MouseButtonModifierFlags.MOUSE_BUTTON_SGR_RELEASE_FLAG) != 0) {
          // for mouse release event
          sb.append(String.format("\033[<%d;%d;%dm",
                                  button ^ MouseButtonModifierFlags.MOUSE_BUTTON_SGR_RELEASE_FLAG,
                                  x,
                                  y));
        }
        else {
          // for mouse press/motion event
          sb.append(String.format("\033[<%d;%d;%dM", button, x, y));
        }
        break;
      case MOUSE_FORMAT_XTERM:
      default:
        // X10 compatibility mode requires ASCII
        // US-ASCII is only 7 bits, so we use ISO-8859-1 (8 bits with ASCII transparency)
        // to handle positions greater than 95 (= 127-32)
        charset = "ISO-8859-1";
        sb.append(String.format("\033[M%c%c%c", (char) (32 + button), (char) (32 + x), (char) (32 + y)));
        break;
    }
    LOG.debug(mouseFormat + " (" + charset + ") report : " + button + ", " + x + "x" + y + " = " + sb);
    return sb.toString().getBytes(Charset.forName(charset));
  }

  private byte[] encodeWheel(@NotNull MouseWheelEvent event,
                                      Terminal terminal,
                                      @NotNull MouseEventProcessingSettings settings){

    if (settings.isUsingAlternateBuffer() &&
      settings.isSimulateMouseScrollWithArrowKeysInAlternateScreen() &&
      !event.isShiftDown()/* skip horizontal scrolls */
    ) {
      //Send Arrow keys instead
      Integer key;
      if (event.getButtonCode() == MouseButtonCodes.SCROLLUP) {
        key = KeyEvent.VK_UP;
      }
      else if (event.getButtonCode() == MouseButtonCodes.SCROLLDOWN) {
        key = KeyEvent.VK_DOWN;
      }
      else {
        key = null;
      }
      if (key != null) {
        byte[] arrowKeys = terminal.getCodeForKey(key, 0);
        int repeatCount = Math.abs(event.getUnitsToScroll());
        if (arrowKeys == null || repeatCount == 0) {
          return null;
        }

        byte[] result = new byte[arrowKeys.length * repeatCount];
        for (int i = 0; i < repeatCount; i++) {
          System.arraycopy(arrowKeys, 0, result, i * arrowKeys.length, arrowKeys.length);
        }
        return result;
      }
    }
    return null;
  }

  private boolean isRemoteMouseAction(MouseEvent event, MouseMode mode){
    return mode != MouseMode.MOUSE_REPORTING_NONE && !event.isShiftDown();
  }
}
