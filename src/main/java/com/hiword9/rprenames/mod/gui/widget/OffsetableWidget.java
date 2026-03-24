package com.hiword9.rprenames.mod.gui.widget;

import net.minecraft.client.gui.layouts.LayoutElement;

public interface OffsetableWidget extends Offsetable, LayoutElement {
    @Override
    default void offset(int x, int y) {
        offset(this, x, y);
    }

    static void offset(LayoutElement widget, int x, int y) {
        widget.setPosition(widget.getX() + x, widget.getY() + y);
    }
}
