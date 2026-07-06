package com.jediterm.terminal.emulator.mouse;

import com.jediterm.core.input.MouseEvent;
import com.jediterm.core.input.MouseWheelEvent;
import org.jetbrains.annotations.NotNull;

public interface TerminalMouseListener {
  boolean onMouseEvent(int x, int y, @NotNull MouseEvent event, MouseEventProcessingSettings settings);
}
