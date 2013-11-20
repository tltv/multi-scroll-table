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

import static org.vaadin.tltv.multiscrolltable.client.ui.TableUtil.defaultString;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_BUFFERSIZE;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_CAPTION;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_CHECK_SPACE_AVAILABLE;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_CHILDRENS_ALLOWED;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_COLS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_COLUMN_STRUCTURE_CHANGED;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_DEPTH;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_DESCRIPTION;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_IMMEDIATE;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_INDEX;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_OPEN;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_PID;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_READONLY;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_REQFIRSTCOL;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_REQFIRSTROW;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_ROWS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_ROWS_CHANGED;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_ROW_STRUCTURE_CHANGED;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_SCROLL_GROUPS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.ATTR_TOTALROWS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_COLUMN;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_COLUMNGROUP;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_COLUMNS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_ROWS;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_SCROLLCONTENT;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_TR;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.TAG_VALUE;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.VAR_NEWVALUE;
import static org.vaadin.tltv.multiscrolltable.client.ui.VCustomScrollTable.VAR_TOGGLE_COLLAPSED;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vaadin.tltv.multiscrolltable.client.MultiScrollTableServerRpc;
import org.vaadin.tltv.multiscrolltable.client.MultiScrollTableState;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Hierarchical;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.KeyMapper;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.LegacyComponent;

public class CustomScrollTable extends AbstractComponent implements
        LegacyComponent, ValueChangeListener, ItemSetChangeListener,
        PropertySetChangeListener, ScrollContentChangeListener {

    private static final long serialVersionUID = 4875429342703360229L;

    private static final int ROW_BUFFER_SIZE = 5;

    /**
     * Keymapper for proptery ids.
     */
    protected final KeyMapper columnIdMap = new KeyMapper();

    private final Set<ScrollContent> scrollContents = new LinkedHashSet<ScrollContent>();

    protected int requestedRowsToPaint = 5;

    // requestedFirstRowToPaint is always a index from the datasource container.
    protected int requestedFirstRowToPaint = -1;

    protected int requestedFirstColToPaint = 0;

    private Collection<Object> visibleColumns = new LinkedList<Object>();

    protected Object[][] pageBuffer = null;

    protected Hierarchical datasource;

    private Object rowHeaderPropertyId;

    private Object rowDescriptionPropertyId;

    /**
     * When true, during the next paint, client will measure the available space
     * for the rows first and request new data to paint.
     */
    protected boolean measureSpaceForRowsAvailable = true;

    protected boolean columnStructureChanged = true;
    protected boolean rowStructureChanged = true;
    protected boolean rowsChanged = true;

    /*
     * This map's purpose is to keep track of the old values. Every value change
     * will change the value in the map. Key is a property id.
     */
    private final Map<Object, Object> oldValueChangeBuffer = new HashMap<Object, Object>();

    private Object valueChangePropertyId;

    private Object valueChangeItemId;

    private final MultiScrollTableServerRpc rpc = new MultiScrollTableServerRpc() {

        @Override
        public void updateFirstRowIndex(Integer newFirstRowIndex) {
            handleRowVisibilityChange(newFirstRowIndex, null);
        }

        @Override
        public void updateVisibleRowCount(Integer newVisibleRows) {
            handleRowVisibilityChange(null, newVisibleRows);
        }

        @Override
        public void updateFirstRowIndexAndVisibleRowCount(
                Integer newFirstRowIndex, Integer newVisibleRows) {
            handleRowVisibilityChange(newFirstRowIndex, newVisibleRows);
        }

    };

    public interface Formatter extends Serializable {
        char getGroupingSeparator();

        String format(Object number, Object propertyId);

        Number parse(Object value, Object propertyId, Object itemId, Property p)
                throws ParseException;
    }

    private Formatter formatter = DEFAULT_FORMATTER;

    public static final Formatter DEFAULT_FORMATTER = new Formatter() {

        private static final long serialVersionUID = 884515870065895819L;
        private final DecimalFormat decimalFormat = new DecimalFormat();

        {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            decimalFormat.setDecimalFormatSymbols(symbols);
        }

        @Override
        public char getGroupingSeparator() {
            return decimalFormat.getDecimalFormatSymbols()
                    .getGroupingSeparator();
        }

        @Override
        public String format(Object number, Object propertyId) {
            return decimalFormat.format(number);
        }

        @Override
        public Number parse(Object value, Object propertyId, Object itemId,
                Property p) throws ParseException {
            if ("".equals(value) || value == null) {
                return 0;
            }
            return decimalFormat.parse(value.toString());
        }
    };

    private final ScrollContent defaultScrollContent = new ScrollContent();
    private final ColumnGroup defaultColumnGroup = new ColumnGroup();
    {
        defaultScrollContent.addColumnGroup(defaultColumnGroup);
    }

    /*
     * Gets and resets default scroll content. Scroll content will contain a
     * single caption-less ColumnGroup which contains all visible columns.
     */
    private ScrollContent getAndResetDefaultScrollContent() {
        Column c;
        defaultColumnGroup.removeAllColumns();
        for (Object o : getVisibleColumns()) {
            c = new Column(columnIdMap.key(o));
            c.setCaption(o.toString());
            defaultColumnGroup.addColumn(c);
        }
        return defaultScrollContent;
    }

    private interface ContainerStrategy extends Serializable {
        public int size();

        public boolean isNodeOpen(Object itemId);

        public int getDepth(Object itemId);

        public void toggleChildVisibility(Object itemId);

        public void expandNode(Object itemId);

        public void collapseNode(Object itemId);

        public Object getIdByIndex(int index);

        public int indexOfId(Object id);

        public Object nextItemId(Object itemId);

        public Object lastItemId();

        public Object prevItemId(Object itemId);

        public boolean isLastId(Object itemId);

        public Collection<Object> getItemIds();

        public void containerItemSetChange(ItemSetChangeEvent event);
    }

    /**
     * Strategy for Hierarchical but not Collapsible container like
     * {@link HierarchicalContainer}. Store collapsed/open states internally,
     * fool table to use preorder when accessing items from container via
     * Ordered/Indexed methods.
     */
    @SuppressWarnings("serial")
    private class HierarchicalStrategy implements ContainerStrategy {

        private final HashSet<Object> openItems = new HashSet<Object>();

        @Override
        public int getDepth(Object itemId) {
            int depth = 0;
            Hierarchical hierarchicalContainer = getContainerDataSource();
            while (!hierarchicalContainer.isRoot(itemId)) {
                depth++;
                itemId = hierarchicalContainer.getParent(itemId);
            }
            return depth;
        }

        @Override
        public boolean isNodeOpen(Object itemId) {
            return openItems.contains(itemId);
        }

        @Override
        public int size() {
            return getPreOrder().size();
        }

        @Override
        public Collection<Object> getItemIds() {
            return Collections.unmodifiableCollection(getPreOrder());
        }

        @Override
        public boolean isLastId(Object itemId) {
            return itemId.equals(lastItemId());
        }

        @Override
        public Object lastItemId() {
            if (getPreOrder().size() > 0) {
                return getPreOrder().get(getPreOrder().size() - 1);
            } else {
                return null;
            }
        }

        @Override
        public Object nextItemId(Object itemId) {
            int indexOf = getPreOrder().indexOf(itemId);
            if (indexOf == -1) {
                return null;
            }
            indexOf++;
            if (indexOf == getPreOrder().size()) {
                return null;
            } else {
                return getPreOrder().get(indexOf);
            }
        }

        @Override
        public Object prevItemId(Object itemId) {
            int indexOf = getPreOrder().indexOf(itemId);
            indexOf--;
            if (indexOf < 0) {
                return null;
            } else {
                return getPreOrder().get(indexOf);
            }
        }

        @Override
        public void toggleChildVisibility(Object itemId) {
            boolean removed = openItems.remove(itemId);
            if (!removed) {
                openItems.add(itemId);
            }
            clearPreorderCache();
        }

        @Override
        public void expandNode(Object itemId) {
            openItems.add(itemId);
            clearPreorderCache();
        }

        @Override
        public void collapseNode(Object itemId) {
            openItems.remove(itemId);
            clearPreorderCache();
        }

        private void clearPreorderCache() {
            preOrder = null; // clear preorder cache
        }

        List<Object> preOrder;

        /**
         * Preorder of ids currently visible
         * 
         * @return
         */
        private List<Object> getPreOrder() {
            if (preOrder == null) {
                preOrder = new ArrayList<Object>();
                Collection<?> rootItemIds = getContainerDataSource()
                        .rootItemIds();
                for (Object id : rootItemIds) {
                    preOrder.add(id);
                    addVisibleChildTree(id);
                }
            }
            return preOrder;
        }

        private void addVisibleChildTree(Object id) {
            if (isNodeOpen(id)) {
                Collection<?> children = getContainerDataSource().getChildren(
                        id);
                if (children != null) {
                    for (Object childId : children) {
                        preOrder.add(childId);
                        addVisibleChildTree(childId);
                    }
                }
            }

        }

        @Override
        public int indexOfId(Object id) {
            return getPreOrder().indexOf(id);
        }

        @Override
        public Object getIdByIndex(int index) {
            return getPreOrder().get(index);
        }

        @Override
        public void containerItemSetChange(ItemSetChangeEvent event) {
            // preorder becomes invalid on sort, item additions etc.
            clearPreorderCache();
        }

    }

    public CustomScrollTable() {
        this((Container) null);
    }

    public CustomScrollTable(ScrollContent... scrollContents) {
        this(null, scrollContents);
    }

    public CustomScrollTable(Container dataSource,
            ScrollContent... scrollContents) {
        registerRpc(rpc, MultiScrollTableServerRpc.class);

        setContainerDataSource(dataSource);
        if (scrollContents != null && scrollContents.length > 0) {
            for (ScrollContent sc : scrollContents) {
                this.scrollContents.add(sc);
                sc.setScrollContentChangeListener(this);
            }
        } else {
            this.scrollContents.add(getAndResetDefaultScrollContent());
            defaultScrollContent.setScrollContentChangeListener(this);
        }
    }

    @Override
    public MultiScrollTableState getState() {
        return (MultiScrollTableState) super.getState();
    }

    /*
     * ContainerStrategy will handle the correct order for the hierarchical
     * content.
     */
    private ContainerStrategy containerStrategy;

    private ContainerStrategy getContainerStrategy() {
        if (containerStrategy == null) {
            containerStrategy = new HierarchicalStrategy();
        }
        return containerStrategy;
    }

    public void setContainerDataSource(Container dataSource) {
        containerStrategy = null;
        if (dataSource == null) {
            datasource = new HierarchicalContainer();
        } else if (!(dataSource instanceof Hierarchical)) {
            datasource = new ContainerHierarchicalWrapper(dataSource);
        } else {
            datasource = (Hierarchical) dataSource;
        }

        setVisibleColumns(datasource.getContainerPropertyIds());

        ((Property.ValueChangeNotifier) datasource).addListener(this);
        ((Container.ItemSetChangeNotifier) datasource).addListener(this);
        ((Container.PropertySetChangeNotifier) datasource).addListener(this);
    }

    public Hierarchical getContainerDataSource() {
        return datasource;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        final Object[] colids = getVisibleColumns();
        final Object[][] cells = getVisibleCells();
        final int cols = colids.length;

        target.addAttribute(ATTR_IMMEDIATE, isImmediate());
        target.addAttribute(ATTR_COLS, cols);
        target.addAttribute(ATTR_ROWS, requestedRowsToPaint);
        target.addAttribute(ATTR_TOTALROWS, size());
        target.addAttribute(ATTR_REQFIRSTROW, requestedFirstRowToPaint);
        target.addAttribute(ATTR_REQFIRSTCOL, requestedFirstColToPaint);
        target.addAttribute(ATTR_BUFFERSIZE, ROW_BUFFER_SIZE);
        target.addAttribute(ATTR_SCROLL_GROUPS, scrollContents.size());

        boolean doPaintRows = !measureSpaceForRowsAvailable;
        if (measureSpaceForRowsAvailable) {
            target.addAttribute(ATTR_CHECK_SPACE_AVAILABLE, true);
            measureSpaceForRowsAvailable = false;
        }

        paintColumns(target);
        paintFloatingRows(target);
        if (doPaintRows) {
            paintRows(target, cells, cols);
        }

    }

    /*
     * Add column data to the UIDL
     */
    private void paintColumns(PaintTarget target) throws PaintException {
        target.startTag(TAG_COLUMNS);
        if (columnStructureChanged) {
            target.addAttribute(ATTR_COLUMN_STRUCTURE_CHANGED, true);
        }
        int index = 0;

        // Paint all scroll contents
        for (ScrollContent sc : scrollContents) {
            target.startTag(TAG_SCROLLCONTENT);

            // Paint scroll content's column groups
            for (ColumnGroup cg : sc.getColumnGroups()) {
                index = paintColumnGroup(target, cg, index);
            }
            target.endTag(TAG_SCROLLCONTENT);
        }
        target.endTag(TAG_COLUMNS);
        columnStructureChanged = false;
    }

    private int paintColumnGroup(PaintTarget target, ColumnGroup cg, int index)
            throws PaintException {
        target.startTag(TAG_COLUMNGROUP);
        target.addAttribute(ATTR_CAPTION, defaultString(cg.getCaption()));

        if (cg instanceof HierarchicalColumnGroup) {
            // Paint column group's hierarchical structure
            for (ColumnGroup subCg : ((HierarchicalColumnGroup) cg)
                    .getSubColumnGroups()) {
                index = paintColumnGroup(target, subCg, index);
            }
        } else {
            // Paint column group's columns
            for (Column c : cg.getColumns()) {
                paintColumn(target, c, index++);
            }
        }
        target.endTag(TAG_COLUMNGROUP);

        return index;
    }

    private void paintColumn(PaintTarget target, Column c, int index)
            throws PaintException {
        target.startTag(TAG_COLUMN);
        target.addAttribute(ATTR_PID, columnIdMap.key(c.getColumnId()));
        target.addAttribute(ATTR_INDEX, index);
        target.addAttribute(ATTR_CAPTION, defaultString(c.getCaption()));
        target.addAttribute(ATTR_READONLY, c.isReadonly() || isReadOnly());
        // TODO

        target.endTag(TAG_COLUMN);
    }

    private void paintRows(PaintTarget target, Object[][] cells, int cols)
            throws PaintException {
        int index = 0;
        // Add rows and cell values to the UIDL
        if (cells != null && cols > 0) {
            target.startTag(TAG_ROWS);
            if (rowStructureChanged) {
                target.addAttribute(ATTR_ROW_STRUCTURE_CHANGED, true);
            } else if (rowsChanged) {
                target.addAttribute(ATTR_ROWS_CHANGED, true);
            }

            int size = size();
            int end = cells[0].length;
            if (end > size) {
                end = size;
            }
            String v;
            for (int i = 0; i < end; i++) {
                index = (Integer) cells[0][i];

                target.startTag(TAG_TR);
                target.addAttribute(ATTR_INDEX, index);
                Object itemId = ((Indexed) datasource).getIdByIndex(index);

                String rowHeader = getRowHeaderByIndex(index);
                if (rowHeader != null) {
                    target.addAttribute(ATTR_CAPTION, rowHeader);
                }
                String rowDescription = getRowDescriptionByIndex(index);
                if (rowDescription != null && rowDescription.length() > 0) {
                    target.addAttribute(ATTR_DESCRIPTION, rowDescription);
                }

                target.addAttribute(ATTR_DEPTH, getContainerStrategy()
                        .getDepth(((Indexed) datasource).getIdByIndex(index)));

                if (getContainerDataSource().areChildrenAllowed(itemId)) {
                    target.addAttribute(ATTR_CHILDRENS_ALLOWED, true);
                    target.addAttribute(ATTR_OPEN, getContainerStrategy()
                            .isNodeOpen(itemId));
                }

                for (int j = 1; j < (cols + 1); j++) {
                    v = (String) cells[j][i];
                    if (v == null) {
                        v = "";
                    }
                    target.startTag(TAG_VALUE);
                    target.addText(v);
                    target.endTag(TAG_VALUE);
                }

                target.endTag(TAG_TR);
            }
            target.endTag(TAG_ROWS);
        }
        rowStructureChanged = false;
        rowsChanged = false;
    }

    private void paintFloatingRows(PaintTarget target) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.AbstractComponent#changeVariables(java.lang.Object,
     * java.util.Map)
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {

        // TODO replcae with RPC
        handleNewValueChange(variables);
        handleHierarchyChange(variables);

    }

    /* Handle hierarchy change. */
    private void handleHierarchyChange(Map<String, Object> variables) {
        if (variables.containsKey(VAR_TOGGLE_COLLAPSED)) {
            // Handle row's hierarchy collapsing/expanding
            // Client returns a index that comes from the items container.
            Integer rowIndex = (Integer) variables.get(VAR_TOGGLE_COLLAPSED);
            Object itemId = ((Indexed) datasource).getIdByIndex(rowIndex);
            toggleChildVisibility(itemId);
        }
    }

    /* Handle cell value change. */
    private void handleNewValueChange(Map<String, Object> variables) {
        if (variables.containsKey(VAR_NEWVALUE)) {
            // Cell value has changed.
            String[] s = (String[]) variables.get(VAR_NEWVALUE);
            handleValueChange(s);
        }
    }

    /* Handle change of first visible row and/or visible row count. */
    private void handleRowVisibilityChange(Integer newFirstRowIndex,
            Integer newVisibleRows) {
        if (newFirstRowIndex == null && newVisibleRows == null) {
            return;
        }

        boolean doRefresh = false;
        if (newFirstRowIndex != null) {
            // requestedFirstRowToPaint is always a index from the data
            // source.
            if (newFirstRowIndex >= size()) {
                newFirstRowIndex = size() - 1;
            }
            if (newFirstRowIndex < 0) {
                newFirstRowIndex = 0;
            }
            if (getContainerStrategy().size() > newFirstRowIndex) {
                Object id = getContainerStrategy().getIdByIndex(
                        newFirstRowIndex);
                int newReqFirstRowToPaint = ((Indexed) datasource)
                        .indexOfId(id);
                if (newReqFirstRowToPaint != requestedFirstRowToPaint) {
                    requestedFirstRowToPaint = newReqFirstRowToPaint;
                    rowsChanged = true;
                    doRefresh = true;
                }
            } else {
                requestedFirstRowToPaint = 0;
            }
        }

        if (newVisibleRows != null) {
            requestedRowsToPaint = newVisibleRows.intValue();
            rowStructureChanged = true;
            doRefresh = true;
        }
        if (doRefresh) {
            requestRefreshDataToPaint();
        }
    }

    private void toggleChildVisibility(Object itemId) {
        getContainerStrategy().toggleChildVisibility(itemId);
        rowStructureChanged = true;
        requestRefreshDataToPaint();
    }

    private void handleValueChange(String[] s) {
        if (s != null && s[0] != null && s[1] != null) {
            if (isReadOnly()) {
                markAsDirty();
                return;
            }
            // Client returns a index that comes from the items container.
            int rowIndex = Integer.parseInt(s[1]);
            if (rowIndex == -1) {
                // TODO Implement total rows
                // Header value changed
                // updateHeaderTotalValue(Integer.parseInt(s[0]), s[2]);
            } else {
                setPropertyValue(s[0], rowIndex, s[2]);
            }
        }
    }

    private void formatAndSetNewValue(Property p, Object propertyId,
            Object itemId, String newValue) {
        String preformattedValue = newValue;
        if (Number.class.isAssignableFrom(p.getType())) {
            try {
                Number n = formatter.parse(preformattedValue, propertyId,
                        itemId, p);
                setPropertyValue(p, propertyId, itemId, n);
                pageBuffer = null;

                // Check the format. If it needs to be fixed, send a repaint
                // request.
                String formatted = formatter.format(n, propertyId);
                if (!preformattedValue.equals(formatted)) {
                    markAsDirty();
                }
            } catch (ParseException e) {
                // Invalid value. Request repaint to show the old value.
                markAsDirty();
            }

        } else {
            setPropertyValue(p, propertyId, itemId, preformattedValue);
        }
    }

    private void setPropertyValue(String pId, int rowIndex, String newValue) {
        Object propertyId = columnIdMap.get(pId);
        Object itemId = ((Indexed) datasource).getIdByIndex(rowIndex);
        Property p = datasource.getContainerProperty(itemId, propertyId);
        formatAndSetNewValue(p, propertyId, itemId, newValue);
    }

    /**
     * Set internal value for the target property. It is recommended to set
     * property values by this method, when it is necessary to handle table's
     * internal states like the value change would have been triggered by the
     * client.
     * 
     * @param p
     *            Target property in the container datasource
     * @param propertyId
     *            Target propery's property id
     * @param itemId
     *            Target property's item id
     * @param newVal
     *            New value object
     */
    public void setPropertyValue(Property p, Object propertyId, Object itemId,
            Object newVal) {
        if (p == null) {
            return;
        }

        oldValueChangeBuffer.put(propertyId, p.getValue());
        valueChangePropertyId = propertyId;
        valueChangeItemId = itemId;

        p.setValue(newVal);
    }

    /**
     * Expand a target row hierarchy.
     * 
     * @param itemId
     */
    public void expandNode(Object itemId) {
        getContainerStrategy().expandNode(itemId);
        markAsDirty();
    }

    /**
     * Collapse a target row hierarchy.
     * 
     * @param itemId
     */
    public void collapseNode(Object itemId) {
        getContainerStrategy().collapseNode(itemId);
        markAsDirty();
    }

    /**
     * Get the row header property id.
     * 
     * @return Row header property id
     */
    public Object getRowHeaderItemId() {
        return rowHeaderPropertyId;
    }

    /**
     * Set the row header property id.
     * 
     * @param rowHeaderPropertyId
     *            Row header property id
     */
    public void setRowHeaderPropertyId(Object rowHeaderPropertyId) {
        this.rowHeaderPropertyId = rowHeaderPropertyId;
        setVisibleColumns(visibleColumns.toArray());
    }

    /**
     * Get the row description id.
     * 
     * @return Row description id
     */
    public Object getRowDescriptionPropertyId() {
        return rowDescriptionPropertyId;
    }

    /**
     * Set the row description property id.
     * 
     * @param rowDescriptionPropertyId
     *            Row description property id
     */
    public void setRowDescriptionPropertyId(Object rowDescriptionPropertyId) {
        this.rowDescriptionPropertyId = rowDescriptionPropertyId;
        setVisibleColumns(visibleColumns.toArray());
    }

    /**
     * Returns property ids of visible columns.
     * 
     * @return Visible columns
     */
    public Object[] getVisibleColumns() {
        if (visibleColumns == null) {
            return null;
        }
        return visibleColumns.toArray();
    }

    private Object[][] getVisibleCells() {
        if (pageBuffer == null) {
            refreshRenderedCells();
        }
        return pageBuffer;
    }

    /**
     * Set the Collection of property identifiers that will be visible in the
     * table. Setting a value by this method will refresh the table content.
     * Unless its a null, which will throw a NullPointerException. Or
     * IllegalArgumentException when it doesn't exist in the container.
     * 
     * @param newVisibleColumns
     *            Collection of property identifiers.
     */
    public void setVisibleColumns(Collection<?> newVisibleColumns) {
        setInternalVisibleColumns(newVisibleColumns);
    }

    /**
     * Set the array of property identifiers that will be visible in the table.
     * Setting a value by this method will refresh the table content. Unless its
     * a null, which will throw a NullPointerException. Or
     * IllegalArgumentException when it doesn't exist in the container.
     * 
     * @param newVisibleColumns
     *            Array of property identifiers.
     */
    public void setVisibleColumns(Object[] newVisibleColumns) {
        // Visible columns must exist
        if (newVisibleColumns == null) {
            throw new NullPointerException(
                    "Can not set visible columns to null value");
        }
        setInternalVisibleColumns(Arrays.asList(newVisibleColumns));
    }

    protected void setInternalVisibleColumns(Collection<?> newVisibleColumns) {
        // Visible columns must exist
        if (newVisibleColumns == null) {
            throw new NullPointerException(
                    "Can not set visible columns to null value");
        }

        // Checks that the new visible columns contains no nulls and properties
        // exist
        final Collection<?> properties = datasource.getContainerPropertyIds();
        for (Object propertyCandidate : newVisibleColumns) {
            if (!properties.contains(propertyCandidate)) {
                throw new IllegalArgumentException(
                        "Property ids must exist in the Container, missing id: "
                                + propertyCandidate);
            }
        }

        // Check for column changes
        if (visibleColumns == null
                || newVisibleColumns.size() != visibleColumns.size()
                || !newVisibleColumns.containsAll(visibleColumns)) {
            requestRefreshDataToPaint();
        }

        visibleColumns = Collections.unmodifiableCollection(newVisibleColumns);
    }

    /**
     * Refresh data (to be painted) instantly.
     */
    public void refreshData() {
        fixRequestedFirstColumnToPaint();
        fixRequestedFirstRowToPaint();
        refreshRenderedCells();
    }

    /**
     * Add a new ScrollContent for this table. Columns in a ScrollContent will
     * share a same vertical scroll-bar, but will have a separate horizontal
     * scroll-bars.
     * 
     * @param scrollContent
     * @return
     */
    public boolean addScrollContent(ScrollContent scrollContent) {
        // Add the new scroll content
        boolean added = scrollContents.add(scrollContent);
        if (added) {
            // Remove default scroll content
            if (scrollContents.contains(defaultScrollContent)) {
                scrollContents.remove(defaultScrollContent);
            }
            // add columns to visible columns list
            LinkedList<Object> newVisibleColumns = new LinkedList<Object>(
                    Arrays.asList(getVisibleColumns()));
            newVisibleColumns.addAll(scrollContent.getColumnIds());
            setVisibleColumns(newVisibleColumns);

            scrollContent.setScrollContentChangeListener(this);
            scrollContentChanged();
        }
        return added;
    }

    /**
     * Returns all scroll contents that are given for this table.
     * 
     * @return Set of scroll contents
     */
    public Set<ScrollContent> getScrollContents() {
        if (!scrollContents.contains(defaultScrollContent)) {
            return Collections.unmodifiableSet(scrollContents);
        }
        return Collections.emptySet();
    }

    /**
     * Remove the target ScrollContent.
     * 
     * @param scrollContent
     * @return
     */
    public boolean removeScrollContent(ScrollContent scrollContent) {
        boolean removed = scrollContents.remove(scrollContent);
        if (removed) {
            scrollContent.setScrollContentChangeListener(null);
            if (scrollContents.size() == 0) {
                // Add a default scroll content
                scrollContents.add(getAndResetDefaultScrollContent());
            }
            scrollContentChanged();
        }
        return removed;
    }

    /**
     * Request data to be painted to be refreshed on next paint call.
     */
    public void requestRefreshDataToPaint() {
        pageBuffer = null;
        markAsDirty();
    }

    private void refreshRenderedCells() {
        final Object[] colids = getVisibleColumns();
        final int cols = colids.length;
        if (requestedFirstRowToPaint < 0) {
            resetRequestedFirstRowToPaint();
        }
        int firstIndex = requestedFirstRowToPaint;

        int totalRows = size();
        int realCols = cols + 1;
        if (totalRows == 0) {
            Object[][] cells = new Object[realCols][totalRows];
            pageBuffer = cells;
            return;
        }

        // ContainerStrategy knows the real ordered index for the item.
        int orderedIndex = getContainerStrategy().indexOfId(
                ((Indexed) datasource).getIdByIndex(firstIndex));

        int size = totalRows;
        // As many as ROW_BUFFER_SIZE rows will be added to the start and
        // end of the content. Depending on the current position
        // (=orderedIndex).
        int reqRowsWithBuffer = requestedRowsToPaint + (2 * ROW_BUFFER_SIZE); // Initial
        // Adjust the end part of the row buffer.
        if ((orderedIndex + requestedRowsToPaint + ROW_BUFFER_SIZE) > totalRows) {
            reqRowsWithBuffer -= (orderedIndex + requestedRowsToPaint + ROW_BUFFER_SIZE)
                    - totalRows;
        }
        // Adjust the starting part of the row buffer.
        if (orderedIndex < ROW_BUFFER_SIZE) {
            int topBuffer = ROW_BUFFER_SIZE - (ROW_BUFFER_SIZE - orderedIndex);
            orderedIndex -= topBuffer;
            reqRowsWithBuffer -= topBuffer;
        } else {
            orderedIndex -= ROW_BUFFER_SIZE;
        }

        if (reqRowsWithBuffer > 0 && reqRowsWithBuffer < size) {
            size = reqRowsWithBuffer;
        }

        // Notice buffer size
        if (orderedIndex < 0) {
            orderedIndex = 0;
        }
        firstIndex = ((Indexed) datasource).indexOfId(getContainerStrategy()
                .getIdByIndex(orderedIndex));
        if ((orderedIndex + size) >= totalRows) {
            // Fix firstIndex when it exceeds the actual size (=totalRows)
            int indexFix = (totalRows - size);
            firstIndex = ((Indexed) datasource)
                    .indexOfId(getContainerStrategy().getIdByIndex(indexFix));
        }

        Object[][] cells = new Object[realCols][size];

        Object id = ((Indexed) datasource).getIdByIndex(firstIndex);
        int index = firstIndex;
        Object value;
        for (int i = 0; i < size && id != null; i++) {

            if (cols > 0) {
                cells[0][i] = index;
                for (int j = 0; j < cols; j++) {
                    Property p = datasource.getContainerProperty(id, colids[j]);
                    value = getPropertyValue(id, p, colids[j]);
                    cells[j + 1][i] = value;
                }
            }

            id = getContainerStrategy().nextItemId(id);
            index = ((Indexed) datasource).indexOfId(id);
        }

        pageBuffer = cells;
    }

    private String getRowHeaderByIndex(int i) {
        if (rowHeaderPropertyId == null) {
            return null;
        }
        Object id = ((Indexed) datasource).getIdByIndex(i);
        Item item = datasource.getItem(id);
        if (item == null) {
            return null;
        }
        return item.getItemProperty(rowHeaderPropertyId).toString();
    }

    private String getRowDescriptionByIndex(int i) {
        if (rowDescriptionPropertyId == null) {
            return null;
        }
        Object id = ((Indexed) datasource).getIdByIndex(i);
        Item item = datasource.getItem(id);
        if (item == null) {
            return null;
        }
        Object v = item.getItemProperty(rowDescriptionPropertyId).getValue();
        if (v != null) {
            return v.toString();
        }
        return null;
    }

    /**
     * Returns size of the data source.
     * 
     * @return Total rows
     */
    public int size() {
        return getContainerStrategy().size();
    }

    protected Object getPropertyValue(Object rowId, Property property,
            Object propertyId) {
        return formatPropertyValue(rowId, property, propertyId);
    }

    protected String formatPropertyValue(Object rowId, Property property,
            Object propertyId) {
        if (property == null || property.getValue() == null) {
            return "";
        }

        if (property.getType() != null
                && Number.class.isAssignableFrom(property.getType())) {
            try {
                return formatter.format(property.getValue(), propertyId);
            } catch (Exception e) {
                return "0";
            }
        }
        return property.toString();
    }

    /*
     * Fixes requestedFirstRowToPaint datasource item index. If index doesn't
     * exist (overflows), index will be set to the index of the first item in
     * the datasource.
     */
    private void fixRequestedFirstRowToPaint() {
        if (requestedFirstRowToPaint != -1) {
            if (getContainerStrategy() != null && size() > 0) {
                Object id = ((Indexed) datasource)
                        .getIdByIndex(requestedFirstRowToPaint);
                // Check that target item is still visible
                boolean existAndIsVisible = id != null
                        && getContainerStrategy().indexOfId(id) != -1;
                if (!existAndIsVisible) {
                    // When its not, requestedFirstRowToPaint needs to updated
                    // Reset content to start from the first item
                    resetRequestedFirstRowToPaint();
                }
            } else {
                requestedFirstRowToPaint = 0;
            }
        }
    }

    /*
     * Resets the index of first requested row to be painted.
     */
    private void resetRequestedFirstRowToPaint() {
        if (getContainerStrategy() != null && size() > 0) {
            Object id = getContainerStrategy().getIdByIndex(0);
            requestedFirstRowToPaint = ((Indexed) datasource).indexOfId(id);
        } else {
            requestedFirstRowToPaint = 0;
        }
    }

    /*
     * Fixes requestedFirstColToPaint datasource property index. If index
     * doesn't exist (overflows), index will be set to the index of the first
     * property in the datasource.
     */
    private void fixRequestedFirstColumnToPaint() {
        if (requestedFirstColToPaint != -1) {
            if (requestedFirstColToPaint >= visibleColumns.size()) {
                resetRequestedFirstColumnToPaint();
            }
        }
    }

    /*
     * Resets the index of first requested column to be painted.
     */
    private void resetRequestedFirstColumnToPaint() {
        requestedFirstColToPaint = 0;
    }

    /**
     * Catch a value change and calculate a new total sum for the target column.
     */
    @Override
    public void valueChange(ValueChangeEvent event) {
        // TODO
    }

    @Override
    public void containerItemSetChange(ItemSetChangeEvent event) {
        if (containerStrategy != null) {
            containerStrategy.containerItemSetChange(event);
        }

        rowStructureChanged = true;
        requestRefreshDataToPaint();
    }

    @Override
    public void containerPropertySetChange(PropertySetChangeEvent event) {
        requestRefreshDataToPaint();
    }

    @Override
    public void scrollContentChanged() {
        columnStructureChanged = true;
        requestRefreshDataToPaint();
    }
}
