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

package org.vaadin.tltv.multiscrolltable.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ScrollContent {

    private ScrollContentChangeListener scrollContentChangeListener;

    private final Set<ColumnGroup> columnGroups = new LinkedHashSet<ColumnGroup>();

    /**
     * Add a new ColumnGroups. Scroll content change event will be fired when
     * adding a non-existing ColumnGroup.
     * 
     * @param columnGroup
     */
    public void addColumnGroup(ColumnGroup columnGroup) {
        columnGroup.setScrollContent(this);
        boolean added = columnGroups.add(columnGroup);
        if (added) {
            fireScrollContentChange();
        }
    }

    /**
     * Remove target ColumnGroup. Scroll content change event will be fired when
     * removing a existing ColumnGroup.
     * 
     * @param columnGroup
     */
    public void removeColumnGroup(ColumnGroup columnGroup) {
        columnGroup.setScrollContent(null);
        boolean removed = columnGroups.remove(columnGroup);
        if (removed) {
            fireScrollContentChange();
        }
    }

    /**
     * Returns ordered set of Columns in this scroll content.
     * 
     * @return
     */
    public Set<Column> getColumns() {
        Set<Column> columns = new LinkedHashSet<Column>();
        for (ColumnGroup cg : columnGroups) {
            columns.addAll(cg.getColumns());
        }
        return columns;
    }

    /**
     * Returns ordered set of Column id's in the scroll content.
     * 
     * @return
     */
    public Set<Object> getColumnIds() {
        Set<Object> columnIds = new LinkedHashSet<Object>();
        Set<Column> columns = getColumns();
        for (Column c : columns) {
            columnIds.add(c.getColumnId());
        }
        return columnIds;
    }

    /**
     * Get column groups in a unmodifiable List.
     * 
     * @return
     */
    public List<ColumnGroup> getColumnGroups() {
        return Collections.unmodifiableList(new ArrayList<ColumnGroup>(
                columnGroups));
    }

    public void setScrollContentChangeListener(
            ScrollContentChangeListener scrollContentChangeListener) {
        this.scrollContentChangeListener = scrollContentChangeListener;
    }

    public ScrollContentChangeListener getScrollContentChangeListener() {
        return scrollContentChangeListener;
    }

    private void fireScrollContentChange() {
        if (scrollContentChangeListener != null) {
            scrollContentChangeListener.scrollContentChanged();
        }
    }
}
