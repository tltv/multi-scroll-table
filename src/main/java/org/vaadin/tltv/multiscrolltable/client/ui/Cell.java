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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.client.Util;

public class Cell extends Composite {

    public static final String primaryStyleName = "v-ct-cell";

    private boolean editable;
    private int width;
    private int height;
    private int minWidth;

    private Cell prev;
    private Cell next;

    private Object value;
    private boolean valueChanged = true; // for internal purposes only

    public Cell() {
        initWidget(new HTML());
        setStylePrimaryName(primaryStyleName);
    }

    public boolean isEditable() {
        return editable;
    }

    public int getWidth() {
        return width;
    }

    public Cell getPrev() {
        return prev;
    }

    public void setPrev(Cell prev) {
        this.prev = prev;
    }

    public Cell getNext() {
        return next;
    }

    public void setNext(Cell next) {
        this.next = next;
    }

    public void setValue(Object value) {
        checkNewValue(value);
        this.value = value;
        formatValue();
        calculateMinumumWidth();
    }

    private void checkNewValue(Object newValue) {
        valueChanged = (value == null && newValue != null)
                || (newValue == null && value != null)
                || (newValue != null && !newValue.equals(value));
    }

    protected void formatValue() {
        if (getWidget() instanceof HTML) {
            ((HTML) getWidget()).setText((value != null) ? Util
                    .escapeHTML(String.valueOf(value)) : Util.escapeHTML(" "));
        }
    }

    /**
     * Calculates the width needed for the content to be fully visible. But only
     * if the value has changed. </br> Content element needs to be included in
     * the DOM for correct result.
     */
    protected void calculateMinumumWidth() {
        if (!valueChanged) {
            return; // no need to calculate minwidth
        }
        Element measure = DOM.createElement("p");
        measure.getStyle().setPosition(Position.ABSOLUTE);
        measure.getStyle().setProperty("width", "auto");
        measure.getStyle().setProperty("height", getHeight() + "px");
        measure.getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
        measure.setInnerText(((HTML) getWidget()).getText());
        measure.addClassName(primaryStyleName);
        getWidget().getElement().getParentElement().appendChild(measure);
        minWidth = measure.getClientWidth();
        getWidget().getElement().getParentElement().removeChild(measure);
        GWT.log("Cell.minWidth set to " + minWidth);
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
    }

    /**
     * Get minimum width needed for the content to be fully visible.
     * 
     * @return
     */
    public int getMinWidth() {
        return minWidth;
    }
}
