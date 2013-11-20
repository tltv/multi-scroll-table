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

import com.google.gwt.user.client.ui.InsertPanel;
import com.vaadin.client.UIDL;

public interface RowContainer {

    /**
     * Creates a new Row when it doesn't already exist in the related component
     * container and inserts it to the target position.
     * 
     * @param rowIndex
     * @param uidl
     * @return
     */
    Row createRow(int rowIndex, UIDL uidl);

    void setReConstruct(boolean reConstruct);

    /**
     * When true, next createRow(...) call will return a new Row element.
     * Otherwise it will be updated by the new UIDL. Non-existing Row will be
     * always created by createRow.
     * 
     * @return
     */
    boolean isReConstruct();

    /**
     * Returns a list of current available rows.
     */
    LinkedList<Row> getRows();

    /**
     * Set related insert-able panel that will be updated by createRow(...)
     * method call.
     * 
     * @param relatedInsertablePanel
     */
    void setRelatedPanel(InsertPanel.ForIsWidget relatedInsertablePanel);

    /**
     * Set the scroll handler that will help handle of all things related to
     * scrolling.
     * 
     * @param scrollableContent
     */
    void setScrollableContent(ScrollableContent scrollableContent);

    /**
     * Set a fixed height for the rows.
     * 
     * @param rowHeight
     */
    void setRowHeight(int rowHeight);

    int getRowHeight();

    /**
     * Rows can be realated to some header.
     * 
     * @param headerContainer
     */
    void setHeaderContainer(HeaderContainer headerContainer);
}
