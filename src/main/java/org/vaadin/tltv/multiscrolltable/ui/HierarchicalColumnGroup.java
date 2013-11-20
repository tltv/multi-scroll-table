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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class HierarchicalColumnGroup extends ColumnGroup {

    private ColumnGroup parent;

    private final Set<ColumnGroup> subColumnGroups = new LinkedHashSet<ColumnGroup>();

    /**
     * Get parent column group.
     * 
     * @return
     */
    public ColumnGroup getParent() {
        return parent;
    }

    /**
     * Set a new parent column group.
     * 
     * @param parent
     */
    public void setParent(ColumnGroup parent) {
        this.parent = parent;
    }

    /**
     * Add a new child column group.
     * 
     * @param columngroup
     */
    public void addColumnGroup(ColumnGroup columngroup) {
        if (columngroup instanceof HierarchicalColumnGroup) {
            ((HierarchicalColumnGroup) columngroup).setParent(this);
        }
        boolean added = subColumnGroups.add(columngroup);
        if (added) {
            fireScrollContentChange();
        }
    }

    /**
     * Remove a child column group.
     * 
     * @param columngroup
     * @return
     */
    public boolean removeColumnGroup(ColumnGroup columngroup) {
        if (columngroup instanceof HierarchicalColumnGroup) {
            ((HierarchicalColumnGroup) columngroup).setParent(null);
        }
        boolean removed = subColumnGroups.remove(columngroup);
        if (removed) {
            fireScrollContentChange();
        }
        return removed;
    }

    @Override
    public void addColumn(Column column) {
        throw new UnsupportedOperationException(
                "Adding columns to HieararchicalColumnGroup is not supported. Only ColumnGroups are allowed for adding.");
    }

    @Override
    public boolean removeColumn(Column column) {
        throw new UnsupportedOperationException(
                "Removing columns from HieararchicalColumnGroup is not supported.");
    }

    /**
     * Return a unmodifiable Set of columns.
     * 
     * @return Unmodifiable Set
     */
    @Override
    public Set<Column> getColumns() {
        Set<Column> columns = new HashSet<Column>();
        for (ColumnGroup cg : subColumnGroups) {
            columns.addAll(cg.getColumns());
        }
        return Collections.unmodifiableSet(columns);
    }

    /**
     * Return a unmodifiable Set of sub column groups.
     * 
     * @return
     */
    public Set<ColumnGroup> getSubColumnGroups() {
        return Collections.unmodifiableSet(subColumnGroups);
    }
}
