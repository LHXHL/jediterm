package com.jediterm.terminal.emulator

import com.jediterm.terminal.ArrayTerminalDataStream
import org.junit.Assert
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A data stream that blocks for reading once drained, the way a TTY-backed stream does.
 */
internal class BlockingDataStream(text: String) : ArrayTerminalDataStream(text.toCharArray()) {

  private val drainedLatch: CountDownLatch = CountDownLatch(1)
  private val finishedLatch: CountDownLatch = CountDownLatch(1)

  override fun getChar(): Char {
    if (isEmpty) {
      drainedLatch.countDown()
      Assert.assertTrue("Timeout exceeded", finishedLatch.await(60, TimeUnit.SECONDS))
    }
    return super.getChar()
  }

  fun awaitDrained(timeout: Long, unit: TimeUnit): Boolean = drainedLatch.await(timeout, unit)

  fun finish() {
    finishedLatch.countDown()
  }
}
