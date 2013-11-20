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

import java.util.ArrayList;
import java.util.List;

import org.vaadin.tltv.multiscrolltable.client.event.MultiScrollTableEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;

public class VCustomScrollTable extends Composite implements
        ScrollHandlerWidget {

    public static final String TAG_COLUMNS = "cols";
    public static final String TAG_COLUMN = "c";
    public static final String TAG_ROWS = "rws";
    public static final String TAG_TR = "tr";
    public static final String TAG_VALUE = "v";
    public static final String TAG_SCROLLCONTENT = "sc";
    public static final String TAG_COLUMNGROUP = "cg";

    public static final String ATTR_IMMEDIATE = "im";
    public static final String ATTR_BUFFERSIZE = "buffsize";
    public static final String ATTR_REQFIRSTCOL = "fCol";
    public static final String ATTR_REQFIRSTROW = "fRow";
    public static final String ATTR_REQROWS = "reqRows";
    public static final String ATTR_COLS = "cols";
    public static final String ATTR_ROWS = "rows";
    public static final String ATTR_TOTALROWS = "ttlrows";
    public static final String ATTR_CHILDRENS_ALLOWED = "ca";
    public static final String ATTR_OPEN = "open";
    public static final String ATTR_INDEX = "index";
    public static final String ATTR_CAPTION = "cap";
    public static final String ATTR_DESCRIPTION = "desc";
    public static final String ATTR_DEPTH = "depth";
    public static final String ATTR_PID = "pid";
    public static final String ATTR_READONLY = "ro";
    public static final String ATTR_SCROLL_GROUPS = "csize";
    public static final String ATTR_CHECK_SPACE_AVAILABLE = "chck";
    public static final String ATTR_COLUMN_STRUCTURE_CHANGED = "csc";
    public static final String ATTR_ROW_STRUCTURE_CHANGED = "rsc";
    public static final String ATTR_ROWS_CHANGED = "rowc";

    public static final String VAR_TOGGLE_COLLAPSED = "tc";
    public static final String VAR_NEWVALUE = "nValue";

    /* Paintable id */
    private String pid;

    private ApplicationConnection client;

    private final FlowPanel mainPanel;
    private final FlowPanel contentPanel;
    private final FlowPanel headerPanel;

    private HeaderPanel rowHeaderColumnHeaderPanel;
    private ContentPanel rowHeaderPanel;

    private List<HeaderPanel> headers = new ArrayList<HeaderPanel>();
    private List<ContentPanel> contents = new ArrayList<ContentPanel>();

    private final List<FloatingRow> floatingRows = new ArrayList<FloatingRow>();

    // When true, client.updateComponent(...) is in progress.
    private final boolean renderingBase = false;

    private int height = -1;
    private int width = -1;
    private int measuredRowHeight = -1;

    protected final RootUIDLMetaData rootMetaData = new RootUIDLMetaData();

    private boolean reconstructAll = true;

    /* Variables for scrolling */
    private static final int SCROLL_DELAY = 100; // milliseconds
    private int contentVerScrollPos = 0;
    private int scrollingVertically = 0;
    private int activeScrollIndex = 0;

    private MultiScrollTableEventHandler eventHandler;

    protected class RootUIDLMetaData {

        UIDL uidlColumns;
        UIDL uidlRows;

        // Header structure needs to be repainted
        boolean createHeader = false;
        // Header data needs to be updated
        boolean updateHeader = false;
        // Rows need to be repainted
        boolean createContent = false;
        // Rows data need to be updated
        boolean updateContent = false;
        // available space for rows need to be updated
        boolean measureAvailableHeightForContent = false;
        // floating rows need to be repainted
        boolean createFloatingRows = false;
        // floating rows data need to be updated
        boolean updateFloatingRows = false;
        // do only row content needs to be recreated (not the whole layout
        // structure)
        boolean clearContent = false;

        private int contentSize = -1;
        private int bufferSize = -1;
        private int totalRowCount = -1;
        private int requestedRows = -1;

        /**
         * Update metadata by the UIDL
         * 
         * @param uidl
         */
        private void update(UIDL uidl) {
            measureAvailableHeightForContent = reconstructAll
                    || uidl.getBooleanAttribute(ATTR_CHECK_SPACE_AVAILABLE);
            bufferSize = uidl.getIntAttribute(ATTR_BUFFERSIZE);
            totalRowCount = uidl.getIntAttribute(ATTR_TOTALROWS);

            int csize = contentSize;
            contentSize = uidl.getIntAttribute(ATTR_SCROLL_GROUPS);
            requestedRows = uidl.getIntAttribute(ATTR_ROWS);

            uidlColumns = uidl.getChildByTagName(TAG_COLUMNS);
            uidlRows = uidl.getChildByTagName(TAG_ROWS);

            if (reconstructAll || contentSize != csize) {
                createHeader = true;
                createContent = true;
                createFloatingRows = true;
                updateContent = false;
                updateHeader = false;
                updateFloatingRows = false;
                reconstructAll = false;
                clearContent = false;
                return;
            }
            reconstructAll = false;

            if (uidlColumns != null) {
                createHeader = uidlColumns
                        .getBooleanAttribute(ATTR_COLUMN_STRUCTURE_CHANGED);
                updateHeader = !createHeader;
                if (createHeader) {
                    // when header structure has changed, rows needs to be
                    // re-created
                    createContent = true;
                    createFloatingRows = true;
                    return;
                }
            }

            if (uidlRows != null) {
                createContent = uidlRows
                        .getBooleanAttribute(ATTR_ROW_STRUCTURE_CHANGED);
                clearContent = !createContent
                        && uidlRows.getBooleanAttribute(ATTR_ROWS_CHANGED);
                updateContent = !createContent;
            }

            // TODO Floating row meta data?
            // uidlFloatingRows = uidl.getChildByTagName(TAG_FLOATING_ROWS);
            // if (uidlFloatingRows != null) {
            // }
        }

        void clear() {
            uidlColumns = null;
            uidlRows = null;
        }
    }

    public VCustomScrollTable() {
        mainPanel = new FlowPanel();
        initWidget(mainPanel);
        setStylePrimaryName("v-cscrolltable");

        contentPanel = new FlowPanel();
        headerPanel = new FlowPanel();

        mainPanel.add(headerPanel);
        mainPanel.add(contentPanel);
    }

    @Override
    protected void onLoad() {
        reconstructAll = true;
        super.onLoad();
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        pid = uidl.getId();

        rootMetaData.update(uidl);
    }

    public void onStateChanged() {
        updateByMetaData();

        recalculateHeights();
        recalculateWidths();
    }

    private void updateByMetaData() {
        if (rootMetaData.createHeader) {
            headers.clear();
            initHeaderPanels();
            updateHeaderPanels();
        } else if (rootMetaData.updateHeader) {
            updateHeaderPanels();
        }

        if (!rootMetaData.measureAvailableHeightForContent) {
            if (rootMetaData.createContent) {
                contents.clear();
                initContentPanels();
                updateContentPanels();
            } else if (rootMetaData.updateContent) {
                updateContentPanels();
            }

            if (rootMetaData.createFloatingRows) {
                // TODO
            } else if (rootMetaData.updateFloatingRows) {
                // TODO
            }

        } else {
            measureAndSendRequestForVisibleRows();
        }

        rootMetaData.clear();
    }

    /**
     * Update data in header panels
     */
    private void updateHeaderPanels() {
        if (rootMetaData.uidlColumns == null) {
            return;
        }
        int index = 0;
        int c = 0;
        for (HeaderPanel h : headers) {
            h.setFirstColIndexForRowsUidl(c);
            UIDL colUidl = rootMetaData.uidlColumns.getChildUIDL(index);
            h.updateContent(colUidl);
            c += h.getColumnCount();
            index++;
        }
        // TODO Auto-generated method stub
    }

    /**
     * Update data in content panels
     */
    private void updateContentPanels() {
        if (rootMetaData.uidlRows == null) {
            return;
        }
        // Update row header panel
        if (rowHeaderPanel != null) {
            if (rootMetaData.clearContent) {
                rowHeaderPanel.clearContentAndSetReconstructFlagOn();
            }
            rowHeaderPanel.updateContent(rootMetaData.uidlRows);
        }

        ContentPanel lastContentPanel = null; // last content has the vertical
        // scroll-bar.
        if (!contents.isEmpty()) {
            lastContentPanel = contents.get(contents.size() - 1);
        }
        // Update content panels
        for (ContentPanel p : contents) {
            if (rootMetaData.clearContent) {
                p.clearContentAndSetReconstructFlagOn();
            }

            p.updateContent(rootMetaData.uidlRows);
        }

        // update vertical scroll position here. Content with a visible
        // vertical scrollbar may have reseted it's scroll position to zero.
        if (lastContentPanel != null) {
            lastContentPanel.resetVerticalScrollPosition();
        }
    }

    private void initContentPanels() {
        contentPanel.clear();
        rowHeaderPanel = new ContentPanel(new RowHeaderContainer(), this, false);
        rowHeaderPanel.setHeaderContainer(rowHeaderColumnHeaderPanel);
        rowHeaderPanel
                .setRelatedHorizontalScrollable(rowHeaderColumnHeaderPanel);
        contentPanel.add(rowHeaderPanel);
        rowHeaderPanel.initContent(rootMetaData.totalRowCount);

        contents = new ArrayList<ContentPanel>(rootMetaData.contentSize);
        boolean last = false;
        for (int i = 0; i < rootMetaData.contentSize; i++) {
            last = (i == rootMetaData.contentSize - 1);
            // Vertical scroll bar may be visible only in the last content panel
            ContentPanel c = new ContentPanel(this, last);
            c.setHeaderContainer(headers.get(i));
            c.setRelatedHorizontalScrollable(headers.get(i));
            contents.add(i, c);
            contentPanel.add(c);
            c.initContent(rootMetaData.totalRowCount);
        }
    }

    private void initHeaderPanels() {
        headerPanel.clear();
        rowHeaderColumnHeaderPanel = new HeaderPanel() {
            @Override
            public int getColumnCount() {
                return 1;
            }
        };
        headerPanel.add(rowHeaderColumnHeaderPanel);
        rowHeaderColumnHeaderPanel.initContent();

        headers = new ArrayList<HeaderPanel>(rootMetaData.contentSize);
        for (int i = 0; i < rootMetaData.contentSize; i++) {
            HeaderPanel hp = new HeaderPanel();
            headers.add(i, hp);
            headerPanel.add(hp);
            hp.initContent();
        }
    }

    private void initFloatingRows() {
        // TODO
    }

    /**
     * This method measures available height for visible rows. Information will
     * be sent to the server and server will request repaint for visible rows.
     */
    private void measureAndSendRequestForVisibleRows() {
        ContentPanel measure = new ContentPanel(this, false);
        contentPanel.add(measure);
        measure.initContent(rootMetaData.totalRowCount);
        measuredRowHeight = measure.getMeasuredRowHeight();
        eventHandler.onUpdateVisibleRowCount(getHeightAvailable()
                / measuredRowHeight);
    }

    private int getHeightAvailable() {
        if (height < 0) {
            return getElement().getClientHeight();
        }
        return height;
    }

    public void recalculateHeights() {
        GWT.log("Height: " + height);

        if (height > 0) {
            mainPanel.setHeight(height + "px");
        }

        int contentHeight = height;
        int columnPanelHeight = 0;
        for (HeaderPanel cp : headers) {
            columnPanelHeight = Math.max(columnPanelHeight,
                    cp.getCalculatedHeight());
        }
        setHeaderPanelHeights(columnPanelHeight);
        if (contentHeight > 0) {
            // Calculate widget heights when height is not undefined
            contentHeight -= columnPanelHeight;
            if (contentHeight < measuredRowHeight) {
                // Space available for the content is way too small when its
                // under the height of one row.
                contentHeight = measuredRowHeight;
            }
            if (rowHeaderPanel != null) {
                rowHeaderPanel.setHeight(contentHeight);
            }
            for (ContentPanel cp : contents) {
                cp.setHeight(contentHeight);
            }
        }

        // TODO
    }

    private void setHeaderPanelHeights(int height) {
        headerPanel.setHeight(height + "px");
        for (HeaderPanel cp : headers) {
            cp.setHeight(height);
        }
    }

    public void recalculateWidths() {
        GWT.log("Width: " + width);

        // TODO
    }

    /* This timer is scheduled when user stops the vertical scrolling. */
    private final Timer scrollTimer = new Timer() {

        @Override
        public void run() {
            GWT.log("Timer.run after " + SCROLL_DELAY + "ms");
            GWT.log("Scrolling triggers a update");
            eventHandler.onUpdateFirstRowIndex(activeScrollIndex);
        }

    };

    /**
     * Handle scrolling for every widget. Vertical scrolling will also scroll
     * other parts of this widget. Horizontal scrolling does the same but leaves
     * out all the other content panels than the target one.
     * 
     * @param sc
     *            Target ScrollableContent
     * @param horizontalScrollPosition
     *            New horizontal scroll position
     * @param verticalScrollPosition
     *            New vertical scroll position
     * @param forceReset
     *            Force content update by sending a request to the server
     * @param silentScroll
     *            Handle only the client side scrolling without sending any
     *            content update requests to the server. Has effect only to the
     *            target content panel.
     */
    @Override
    public void scrollContent(ScrollableContent sc,
            int horizontalScrollPosition, int verticalScrollPosition,
            boolean forceReset, boolean silentScroll) {
        sc.getRelatedScrollable().setScrollLeft(horizontalScrollPosition);
        if (!silentScroll) {
            if (rowHeaderPanel != null && !sc.equals(rowHeaderPanel)) {
                rowHeaderPanel.setScrollTop(verticalScrollPosition, true);
            }
            for (ContentPanel otherCp : contents) {
                if (otherCp.equals(sc)) {
                    continue;
                }
                otherCp.setScrollTop(verticalScrollPosition, true);
            }
        } else {
            return; // no need to continue
        }

        int prevContentVerScrollPos = contentVerScrollPos;
        contentVerScrollPos = verticalScrollPosition;

        if (forceReset || prevContentVerScrollPos != contentVerScrollPos) {

            int oldIndex = prevContentVerScrollPos / measuredRowHeight;
            activeScrollIndex = contentVerScrollPos / measuredRowHeight;
            int indexDelta = activeScrollIndex - oldIndex;
            scrollingVertically += indexDelta;
            GWT.log("Scroll index delta: " + scrollingVertically);
            if (!forceReset
                    && (getRequestedRows() == getTotalRowCount() || Math
                            .abs(scrollingVertically) <= getBufferSize())) {
                // No need to send scroll request when
                // 1) all rows are visible
                // or 2) content is still visible
                // and previous vertical scroll position is not -1 (-1 is used
                // to force content update)
                return;
            }

            scrollTimer.cancel(); // cancel the active timer

            boolean immediate = forceReset;
            if (immediate) {
                scrollingVertically = 0;
                eventHandler.onUpdateFirstRowIndex(activeScrollIndex);

            } else {
                // re-schedule the timer.
                GWT.log("Scrolling re-schedule");
                scrollTimer.schedule(SCROLL_DELAY);
            }
        }
    }

    @Override
    public boolean isHorizontalScrollbarVisible() {
        for (ContentPanel p : contents) {
            if (p.isHorizontalScrollbarVisible()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isVerticalScrollbarVisible() {
        for (ContentPanel p : contents) {
            if (p.isVerticalScrollbarVisible()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getBufferSize() {
        return rootMetaData.bufferSize;
    }

    private int getTotalRowCount() {
        return rootMetaData.totalRowCount;
    }

    private int getRequestedRows() {
        return rootMetaData.requestedRows;
    }

    @Override
    public VCustomScrollTable getWidget() {
        return (VCustomScrollTable) super.getWidget();
    }

    public int getIntHeight() {
        return height;
    }

    public int getIntWidth() {
        return width;
    }

    public void setIntHeight(int height) {
        this.height = height;
    }

    public void setIntWidth(int width) {
        this.width = width;
    }

    public boolean isRenderingBase() {
        return renderingBase;
    }

    public void setEventHandler(MultiScrollTableEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

}
