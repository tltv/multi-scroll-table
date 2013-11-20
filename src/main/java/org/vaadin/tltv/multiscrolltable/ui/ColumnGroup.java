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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ColumnGroup {

    private String caption;

    private ScrollContent scrollContent;

    private final Set<Column> columns = new LinkedHashSet<Column>();

    /**
     * Get a caption text.
     * 
     * @return text
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Set a new caption text.
     * 
     * @param caption
     *            Text
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * Add a column in this column group. Scroll content change event will be
     * fired when adding a non-existing Column.
     * 
     * @param column
     *            Column to add.
     */
    public void addColumn(Column column) {
        column.setColumnGroup(this);
        boolean added = columns.add(column);
        if (added) {
            fireScrollContentChange();
        }
    }

    /**
     * Remove column from this column group. Scroll content change event will be
     * fired when removing a existing Column.
     * 
     * @param column
     *            Target column.
     * @return
     */
    public boolean removeColumn(Column column) {
        column.setColumnGroup(null);
        boolean removed = columns.remove(column);
        if (removed) {
            fireScrollContentChange();
        }
        return removed;
    }

    /**
     * Removes all columns from this group. Scroll content change event will be
     * fired if any Column is removed.
     */
    public void removeAllColumns() {
        if (columns.size() > 0) {
            boolean removed = false;
            for (Iterator<Column> iterator = columns.iterator(); iterator
                    .hasNext();) {
                Column c = iterator.next();
                c.setColumnGroup(null);
                iterator.remove();
                removed = true;
            }
            if (removed) {
                fireScrollContentChange();
            }
        }
    }

    public ScrollContent getScrollContent() {
        return scrollContent;
    }

    public void setScrollContent(ScrollContent scrollContent) {
        this.scrollContent = scrollContent;
    }

    /**
     * Return a unmodifiable Set of columns.
     * 
     * @return Unmodifiable Set
     */
    public Set<Column> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    protected void fireScrollContentChange() {
        if (getScrollContent() != null
                && getScrollContent().getScrollContentChangeListener() != null) {
            getScrollContent().getScrollContentChangeListener()
                    .scrollContentChanged();
        }
    }
}
