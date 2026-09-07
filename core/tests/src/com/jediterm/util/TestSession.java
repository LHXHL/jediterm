package com.jediterm.util;

import com.jediterm.core.Color;
import com.jediterm.terminal.ArrayTerminalDataStream;
import com.jediterm.terminal.HyperlinkStyle;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.BlockingDataStream;
import com.jediterm.terminal.emulator.Emulator;
import com.jediterm.terminal.emulator.JediEmulator;
import com.jediterm.terminal.model.LinesStorage;
import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.model.hyperlinks.TextProcessing;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestSession {

  private static final int TIMEOUT_SECONDS = 60;

  public static final Color BLUE = new Color(0, 0, 255);

  private final BackBufferTerminal myTerminal;
  private final TextProcessing myTextProcessing;
  private final TerminalTextBuffer myTerminalTextBuffer;
  private final StyleState myStyleState;

  public TestSession(int width, int height) {
    myStyleState = new StyleState();
    TextStyle hyperlinkTextStyle = new TextStyle(TerminalColor.color(BLUE), TerminalColor.WHITE);
    myTextProcessing = new TextProcessing(hyperlinkTextStyle, HyperlinkStyle.HighlightMode.ALWAYS);
    myTerminalTextBuffer = new TerminalTextBuffer(width, height, myStyleState, LinesStorage.DEFAULT_MAX_LINES_COUNT, myTextProcessing);
    myTextProcessing.setTerminalTextBuffer(myTerminalTextBuffer);
    myTerminal = new BackBufferTerminal(myTerminalTextBuffer, myStyleState);
  }

  public @NotNull BackBufferTerminal getTerminal() {
    return myTerminal;
  }

  public @NotNull BackBufferDisplay getDisplay() {
    return myTerminal.getDisplay();
  }

  public @NotNull TerminalTextBuffer getTerminalTextBuffer() {
    return myTerminalTextBuffer;
  }

  public @NotNull TextProcessing getTextProcessing() {
    return myTextProcessing;
  }

  public @NotNull TextStyle getCurrentStyle() {
    return myStyleState.getCurrent();
  }

  public void process(@NotNull String data) throws IOException {
    ArrayTerminalDataStream fileStream = new ArrayTerminalDataStream(data.toCharArray());
    Emulator emulator = new JediEmulator(fileStream, myTerminal);

    while (emulator.hasNext()) {
      emulator.next();
    }
  }

  /**
   * Feeds {@code data} to an emulator running on a separate thread and
   * runs {@code assertions} while that thread is parked waiting for more input.
   */
  public void processAsync(@NotNull String data, @NotNull Runnable assertions) {
    BlockingDataStream stream = new BlockingDataStream(data);
    Emulator emulator = new JediEmulator(stream, myTerminal);
    try (ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "TestSession emulator"))) {
      Future<?> future = executor.submit(() -> {
        while (emulator.hasNext()) {
          try {
            emulator.next();
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      });
      try {
        boolean drained = stream.awaitDrained(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        try {
          if (future.isDone()) {
            awaitCompletion(future);
          }
          Assert.assertTrue("The emulator has not consumed the whole input in " + TIMEOUT_SECONDS + " seconds", drained);
          myTerminalTextBuffer.modify(assertions);
        }
        finally {
          stream.finish();
        }
        awaitCompletion(future);
      }
      finally {
        executor.shutdownNow();
      }
    }
  }

  private static void awaitCompletion(@NotNull Future<?> future) {
    try {
      future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
    catch (TimeoutException e) {
      throw new AssertionError("The emulator has not finished in " + TIMEOUT_SECONDS + " seconds", e);
    }
    catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public void assertCursorPosition(int expectedOneBasedCursorX, int expectedOneBasedCursorY) {
    TestCase.assertEquals(stringifyCursor(expectedOneBasedCursorX, expectedOneBasedCursorY),
      stringifyCursor(myTerminal.getCursorX(), myTerminal.getCursorY()));
  }

  private static @NotNull String stringifyCursor(int cursorX, int cursorY) {
    return "cursorX=" + cursorX + ", cursorY=" + cursorY;
  }
}
