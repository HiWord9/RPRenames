package com.HiWord9.RPRenames.mod.gui.widget;

public interface Offsetable {
    void offset(int x, int y);

    default void offsetX(int x) { offset(x, 0); }

    default void offsetY(int y) { offset(0, y); }
}
