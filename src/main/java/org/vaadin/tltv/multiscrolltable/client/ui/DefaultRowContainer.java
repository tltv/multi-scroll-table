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
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.gwt.user.client.ui.InsertPanel.ForIsWidget;
import com.vaadin.client.UIDL;

public class DefaultRowContainer implements RowContainer {

    protected InsertPanel.ForIsWidget relatedInsertablePanel;
    protected ScrollableContent scrollableContent;
    protected HeaderContainer headerContainer;

    protected int rowHeight = -1;

    protected boolean reConstruct = true;

    protected final LinkedList<Row> rows = new LinkedList<Row>();

    @Override
    public Row createRow(int rowIndex, UIDL rowUidl) {
        Row row = getRow(rowIndex);

        int actualColIndex = 0;
        int startIndex = headerContainer.getFirstColIndexForRowsUidl();
        for (int colIndex = startIndex; colIndex < (startIndex + headerContainer
                .getColumnCount()); colIndex++, actualColIndex++) {
            UIDL columnUIDL = rowUidl.getChildUIDL(colIndex);

            Cell cell = getCell(actualColIndex, row);

            cell.setValue(columnUIDL.getChildString(0));
        }
        return row;
    }

    protected Cell getCell(int colIndex, Row row) {
        Cell cell = null;
        if (isReConstruct()) {
            cell = new Cell();
            cell.setHeight(row.getHeight());
            row.insert(cell, colIndex);
        } else {
            cell = row.getCell(colIndex);
        }
        return cell;
    }

    protected Row getRow(int rowIndex) {
        if (isReConstruct()) {
            Row row = new Row();
            row.setHeight(getRowHeight());
            relatedInsertablePanel.insert(row, rowIndex);
            row.setPosition(Position.RELATIVE);
            // row.setTop(calculateRowTop(rowIndex,
            // scrollableContent.getScrollTop()));
            rows.add(row);
            return row;
        }
        return (Row) relatedInsertablePanel.getWidget(rowIndex);
    }

    protected int calculateRowTop(int rowIndex, int baseContentTop) {
        return (rowHeight * rowIndex) + baseContentTop;
    }

    @Override
    public void setReConstruct(boolean reConstruct) {
        this.reConstruct = reConstruct;
    }

    @Override
    public boolean isReConstruct() {
        return reConstruct;
    }

    @Override
    public LinkedList<Row> getRows() {
        return rows;
    }

    @Override
    public void setRelatedPanel(ForIsWidget relatedInsertablePanel) {
        this.relatedInsertablePanel = relatedInsertablePanel;
    }

    @Override
    public void setScrollableContent(ScrollableContent scrollableContent) {
        this.scrollableContent = scrollableContent;
    }

    @Override
    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    @Override
    public int getRowHeight() {
        return rowHeight;
    }

    @Override
    public void setHeaderContainer(HeaderContainer headerContainer) {
        this.headerContainer = headerContainer;
    }
}
