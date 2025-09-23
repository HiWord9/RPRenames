package com.HiWord9.RPRenames.mod.gui.widget;

import net.minecraft.client.gui.widget.Widget;

public interface OffsetableWidget extends Offsetable, Widget {
    @Override
    default void offset(int x, int y) {
        offset(this, x, y);
    }

    static void offset(Widget widget, int x, int y) {
        widget.setPosition(widget.getX() + x, widget.getY() + y);
    }
}
