/*
 * Copyright 2013 Tomi Virtanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vaadin.tltv.multiscrolltable.client.ui;

import java.util.LinkedList;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class Row extends FlowPanel {

    private int height;

    public Row() {
        setStylePrimaryName("v-ct-row");
    }

    public Cell getCell(int index) {
        return (Cell) getWidget(index);
    }

    public void setCell(Cell cell, int index) {
        insert(cell, index);
    }

    public void setPosition(Position position) {
        getElement().getStyle().setPosition(position);
    }

    public void setTop(int top) {
        getElement().getStyle().setTop(top, Unit.PX);
    }

    /**
     * Get height in pixels previously set by setHeight(int) method.
     * 
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Set height in pixels.
     * 
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
        setHeight(height + "px");
    }

    /**
     * Get minimum width needed for the row to be fully visible.
     * 
     * @return
     */
    public int getMinWidth() {
        int minWidth = 0;
        for (Widget w : getChildren()) {
            if (w instanceof Cell) {
                minWidth += ((Cell) w).getMinWidth();
            }
        }
        return minWidth;
    }

    public LinkedList<Cell> getCells() {
        LinkedList<Cell> cells = new LinkedList<Cell>();
        for (Widget w : getChildren()) {
            if (w instanceof Cell) {
                cells.add((Cell) w);
            }
        }
        return cells;
    }
}
