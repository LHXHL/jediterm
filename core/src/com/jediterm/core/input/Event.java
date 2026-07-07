package com.jediterm.core.input;

final class Event {
  public static final int SHIFT_DOWN_MASK          = 1 << 6;
  public static final int ALT_DOWN_MASK            = 1 << 9;
  public static final int CTRL_DOWN_MASK           = 1 << 7;
  public static final int META_DOWN_MASK           = 1 << 8;
  public static final int ALT_GRAPH_DOWN_MASK      =  1 << 13;
}