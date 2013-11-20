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

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.UIDL;

public class HeaderPanel extends SimplePanel implements HeaderContainer,
        Scrollable {

    private final FlexTable content;

    private int groupHeight = 0;
    private int columnHeight = 0;

    private int levels = 0;
    private int columnCount = 0;
    private int firstColIndexForRowsUidl = 0;

    /*
     * When reConstruct is set to true, updateContent(UIDL) will create new
     * caption elements. If its false, only values will be updated.
     */
    private boolean reConstruct;

    private int[] columnWidths;
    private int calculatedWidth = 0;

    public HeaderPanel() {
        setStylePrimaryName("v-ct-col-headers");
        content = new FlexTable();
        content.setCellSpacing(0);
        content.setCellPadding(0);
        content.getElement().getStyle().setPosition(Position.RELATIVE);
        content.getElement().getStyle().setVerticalAlign(VerticalAlign.BOTTOM);

        // Add element for measuring default height
        HTML measure = new HTML();
        measure.setStylePrimaryName("v-ct-header-group");
        content.setWidget(0, 0, measure);
        measure = new HTML();
        measure.setStylePrimaryName("v-ct-header");
        content.setWidget(0, 1, measure);

        setWidget(content);
    }

    private int getMeasuredGroupHeight() {
        if (groupHeight <= 0) {
            groupHeight = content.getWidget(0, 0).getElement()
                    .getClientHeight();
        }
        return groupHeight;
    }

    private int getMeasuredColumnHeight() {
        if (columnHeight <= 0) {
            columnHeight = content.getWidget(0, 1).getElement()
                    .getClientHeight();
        }
        return columnHeight;
    }

    public void initContent() {
        reConstruct = true;
        calculatedWidth = 0;

        // Measure group default heights before removing measure element
        getMeasuredGroupHeight();
        getMeasuredColumnHeight();

        // Clear content
        content.clear();
        while (content.getRowCount() > 0) {
            content.removeRow(0);
        }
        levels = 0;
    }

    public void updateContent(UIDL uidl) {
        if (uidl == null) {
            return;
        }

        if (uidl.getTag().equals(VCustomScrollTable.TAG_SCROLLCONTENT)) {
            int childcount = uidl.getChildCount();
            if (childcount > 0) {
                ColumnPanel prev = null;
                for (int i = 0; i < childcount; i++) {
                    prev = updateContentByGroupUidl(uidl.getChildUIDL(i), 0, i,
                            prev, null);
                }
            }

        }

        if (reConstruct) {
            columnCount = 0;
            if (content.getRowCount() > 0) {
                columnCount = content.getCellCount(content.getRowCount() - 1);
            }
        }
        reConstruct = false;
    }

    private ColumnPanel updateContentByGroupUidl(UIDL uidl, int level,
            int index, ColumnPanel prevColumn, ColumnPanel group) {
        if (uidl == null) {
            return null;
        }

        String caption = uidl
                .getStringAttribute(VCustomScrollTable.ATTR_CAPTION);
        ColumnPanel p = getColumnPanel(level, index, prevColumn, group);
        updateGroupElement(p);
        p.getLabel().setText(caption);

        boolean updateColSpan = false;
        int childs = uidl.getChildCount();
        if (childs <= 0) {
            return p;
        }
        int cellCount = p.getFirstChildIndex();
        if (reConstruct) {
            // When reconstructing, p.getFirstChildIndex() value is not updated
            // yet.
            try {
                cellCount = content.getCellCount(level + 1);
            } catch (IndexOutOfBoundsException e) {
                cellCount = 0;
            }
        }
        ColumnPanel prev = null;
        for (int i = 0; i < childs; i++) {
            UIDL subUidl = uidl.getChildUIDL(i);
            if (subUidl.getTag().equals(VCustomScrollTable.TAG_COLUMN)) {
                prev = updateContentByColumnUidl(subUidl, level + 1, cellCount
                        + i, prev, p);
                updateColSpan = reConstruct;
            } else if (subUidl.getTag().equals(
                    VCustomScrollTable.TAG_COLUMNGROUP)) {
                prev = updateContentByGroupUidl(subUidl, level + 1, i, prev, p);
            }
        }
        if (updateColSpan) {
            // This needs to be called only for the "leaf" panels.
            p.setColSpan(childs);
        }
        return p;
    }

    private ColumnPanel updateContentByColumnUidl(UIDL uidl, int level,
            int index, ColumnPanel prev, ColumnPanel group) {

        String caption = uidl
                .getStringAttribute(VCustomScrollTable.ATTR_CAPTION);
        ColumnPanel p = getColumnPanel(level, index, prev, group);
        updateColumnElement(p);
        p.getLabel().setText(caption);
        return p;
    }

    /**
     * Get existing ColumnPanel or create and insert a new one.
     * 
     * @param level
     * @param index
     * @param prev
     *            Previous ColumnPanel in the same row
     * @param group
     * @return
     */
    private ColumnPanel getColumnPanel(int level, int index, ColumnPanel prev,
            ColumnPanel group) {
        addRow(level);
        addCell(level, index);

        ColumnPanel p;
        if (reConstruct) {
            p = createColumnElement(prev, level, index);
            if (group != null) {
                p.setParentPanel(group);
            }
            content.setWidget(level, index, p);
        } else {
            p = (ColumnPanel) content.getWidget(level, index);
        }
        return p;
    }

    /**
     * Add a new row to the grid when its not already existing.
     */
    private void addRow(int rowIndex) {
        if (reConstruct) {
            if (rowIndex >= content.getRowCount()) {
                content.insertRow(rowIndex);
                levels++;
            }
        }
    }

    /**
     * Add a new cell to the row if its not already existing.
     * 
     * @param rowIndex
     * @param cellIndex
     */
    private void addCell(int rowIndex, int cellIndex) {
        if (reConstruct) {
            if (cellIndex >= content.getCellCount(rowIndex)) {
                content.insertCell(rowIndex, cellIndex);
            }
        }
    }

    private ColumnPanel createColumnElement(ColumnPanel prevColumn,
            int rowIndex, int colIndex) {
        ColumnPanel p = new ColumnPanel(prevColumn, rowIndex, colIndex);
        p.setStylePrimaryName("v-ct-header-group");
        return p;
    }

    private void updateGroupElement(ColumnPanel p) {
        if (reConstruct) {
            p.setStylePrimaryName("v-ct-header-group");
        }
    }

    private void updateColumnElement(ColumnPanel p) {
        if (reConstruct) {
            p.setStylePrimaryName("v-ct-header");
        }
    }

    public int getCalculatedHeight() {
        return getElement().getClientHeight();
    }

    public int getCalculatedWidth() {
        return getElement().getClientWidth();
    }

    public void setHeight(int height) {
        setHeight(height + "px");
        content.setHeight(height + "px");
    }

    public void setWidth(int width) {
        setWidth(width + "px");
    }

    public class ColumnPanel extends FlowPanel {

        int rowIndex;
        int colIndex;
        final Label label;
        ColumnPanel parentPanel;
        ColumnPanel prevPanel;
        int colspan = 0;
        int firstChildIndex = 0;

        public ColumnPanel(ColumnPanel prevPanel, int rowIndex, int colIndex) {
            this.prevPanel = prevPanel;
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
            label = createLabel();
            add(label);
        }

        private Label createLabel() {
            Label l = new Label();
            l.getElement().getStyle()
                    .setFloat(com.google.gwt.dom.client.Style.Float.LEFT);
            return l;
        }

        public Label getLabel() {
            return label;
        }

        public void setParentPanel(ColumnPanel parentPanel) {
            this.parentPanel = parentPanel;
        }

        public ColumnPanel getParentPanel() {
            return parentPanel;
        }

        public void setColSpan(int colspan) {
            this.colspan += colspan;
            firstChildIndex = colIndex;
            if (prevPanel != null) {
                firstChildIndex = prevPanel.getFirstChildIndex()
                        + prevPanel.getColSpan();
            }
            ((FlexCellFormatter) content.getCellFormatter()).setColSpan(
                    rowIndex, colIndex, this.colspan);
            if (getParentPanel() != null) {
                getParentPanel().setColSpan(this.colspan);
            }
        }

        public int getColSpan() {
            return colspan;
        }

        /**
         * Returns first index of next row's cell which parent this element is.
         * Returned index is valid only, when colspans are set correctly.
         */
        public int getFirstChildIndex() {
            return firstChildIndex;
        }

        public ColumnPanel getPrevPanel() {
            return prevPanel;
        }
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public int getScrollLeft() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setScrollLeft(int pixelsScrolled) {
        content.getElement().getStyle().setLeft(-pixelsScrolled, Unit.PX);
    }

    @Override
    public int getScrollTop() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setScrollTop(int pixelsScrolled) {
        // TODO Auto-generated method stub
    }

    @Override
    public int getFirstColIndexForRowsUidl() {
        return firstColIndexForRowsUidl;
    }

    @Override
    public void setFirstColIndexForRowsUidl(int firstColIndexForRowsUidl) {
        this.firstColIndexForRowsUidl = firstColIndexForRowsUidl;
    }

    @Override
    public int[] setColumnMinWidths(int[] widths) {
        if (content.getRowCount() == 0) {

            if (widths.length == 1) {
                // This works for row header. Header is always empty for that.
                content.setWidth(widths[0] + "px");
            }
            return widths;
        }
        for (int i = 0; i < widths.length; i++) {
            ColumnPanel col = (ColumnPanel) content.getWidget(levels - 1, i);
            int w = col.getOffsetWidth();
            if (widths[i] > w) {
                col.setWidth(widths[i] + "px");
            } else {
                widths[i] = w;
            }
        }
        return widths;
    }
}
