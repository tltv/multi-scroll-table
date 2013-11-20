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
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.vaadin.client.UIDL;
import com.vaadin.client.Util;

public class ContentPanel extends FlowPanel implements ScrollableContent {

    private final ScrollHandlerWidget scrollHandlerWidget;
    private Scrollable relatedHorizontalScrollable;

    private final RowContainer rowContainer;
    private HeaderContainer headerContainer;

    private ScrollPanel scrollPanel;

    private SimplePanel content;
    private FlowPanel contentForRows;

    boolean visibleScrollBarY;

    private int contentTop;

    private int totalRowCount = 0;

    /*
     * When visibleScrollBarY is false, we have to store vertical scroll
     * position here instead of trusting ScrollTable. ScrollTable's vertical
     * scroll bar is hidden when visibleScrollBarY is false. Which means that
     * ScrollTable gives zero scrollTop f.ex. when scrolling horizontally.
     */
    private int verticalScrollPos = 0;
    private boolean nextScrollEventIsSilent = false;
    private int prevHorizontalScrollpos = 0;
    private int prevVerticalScrollpos = 0;

    private final ScrollHandler scrollhandler = new ScrollHandler() {
        @Override
        public void onScroll(ScrollEvent event) {
            int scrollTop = getScrollTop();
            verticalScrollPos = scrollTop;
            boolean horScrolling = prevHorizontalScrollpos != scrollPanel
                    .getHorizontalScrollPosition();
            boolean verScrolling = prevVerticalScrollpos != scrollTop;
            prevHorizontalScrollpos = scrollPanel.getHorizontalScrollPosition();
            prevVerticalScrollpos = scrollTop;
            // Enable silent scrolling only if nextScrollEventIsSilent is
            // true or content is scrolling only horizontally.
            boolean silent = nextScrollEventIsSilent
                    || (!verScrolling && horScrolling);
            scroll(silent, scrollPanel.getHorizontalScrollPosition(),
                    scrollTop, false);
            nextScrollEventIsSilent = false;
        }
    };

    public ContentPanel(ScrollHandlerWidget scrollHandlerWidget,
            boolean visibleScrollBarY) {
        this(new DefaultRowContainer(), scrollHandlerWidget, visibleScrollBarY);
    }

    public ContentPanel(RowContainer rowFactory,
            ScrollHandlerWidget scrollHandlerWidget, boolean visibleScrollBarY) {
        rowContainer = rowFactory;
        this.scrollHandlerWidget = scrollHandlerWidget;
        setStylePrimaryName("v-ct-content");
        initLayout(visibleScrollBarY);
    }

    private void initLayout(boolean visibleScrollBarY) {
        this.visibleScrollBarY = visibleScrollBarY;
        content = new SimplePanel();
        content.setStylePrimaryName("v-ct-content-panel");

        contentForRows = new FlowPanel();
        contentForRows.getElement().getStyle().setPosition(Position.RELATIVE);
        contentForRows.setStylePrimaryName("v-ct-content-panel-for-rows");
        content.setWidget(contentForRows);

        scrollPanel = new ScrollPanel();
        scrollPanel.setStylePrimaryName("v-ct-scroll-panel");
        scrollPanel.setWidget(content);

        if (!visibleScrollBarY) {
            scrollPanel.getElement().getStyle()
                    .setProperty("overflowY", "hidden");
            scrollPanel.getElement().getStyle()
                    .setProperty("overflowX", "auto");
            sinkEvents(Event.ONMOUSEWHEEL);
        } else {
            scrollPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
        }

        scrollPanel.addScrollHandler(scrollhandler);

        // Add one element for measuring row height
        HTML measure = new HTML();
        measure.setStylePrimaryName("v-ct-row");
        contentForRows.add(measure);

        rowContainer.setRelatedPanel(contentForRows);
        rowContainer.setScrollableContent(this);

        add(scrollPanel);
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) == Event.ONMOUSEWHEEL) {
            GWT.log("Scrolling over content panel:  "
                    + event.getMouseWheelVelocityY() * 15);
            scrollVertically(event.getMouseWheelVelocityY() * 15, true, false);
        } else {
            super.onBrowserEvent(event);
        }
    }

    public int getMeasuredRowHeight() {
        if (rowContainer.getRowHeight() < 0) {
            rowContainer.setRowHeight(contentForRows.getWidget(0).getElement()
                    .getClientHeight());
        }
        return rowContainer.getRowHeight();
    }

    public void initContent(int totalRowCount) {
        rowContainer.setReConstruct(true);

        this.totalRowCount = totalRowCount;
        // Measure row height before destroying measure element
        getMeasuredRowHeight();

        // Clear content
        clearContentAndSetReconstructFlagOn();

        updateContentHeight(totalRowCount);
    }

    public void clearContentAndSetReconstructFlagOn() {
        while (contentForRows.getWidgetCount() > 0) {
            contentForRows.remove(0);
        }
        rowContainer.getRows().clear();
        rowContainer.setReConstruct(true);
    }

    public void updateContent(UIDL uidl) {
        if (uidl == null) {
            return;
        }

        int rowCount = uidl.getChildCount();
        int columnCount = (headerContainer != null) ? headerContainer
                .getColumnCount() : 0;
        if (rowCount == 0 || columnCount == 0) {
            return;
        }

        for (int i = 0; i < rowCount; i++) {
            UIDL rowUidl = uidl.getChildUIDL(i);

            Row row = rowContainer.createRow(i, rowUidl);
        }

        updateRowContentTop();
        setInternalContentTop();

        int[] widths = calculateMinWidthsForColumns(rowContainer.getRows());
        // Update headerContainer column widths by the cell widths
        if (headerContainer != null) {
            widths = headerContainer.setColumnMinWidths(widths);
            setColumnMinWidths(rowContainer.getRows(), widths);
        }

        rowContainer.setReConstruct(false);
    }

    private int[] calculateMinWidthsForColumns(List<Row> rows) {
        int[] widths = new int[headerContainer.getColumnCount()];
        int index = 0;
        for (Row r : rows) {
            index = 0;
            for (Cell c : r.getCells()) {
                widths[index] = Math.max(widths[index], c.getMinWidth());
                index++;
            }
        }
        return widths;
    }

    private void setColumnMinWidths(List<Row> rows, int[] widths) {
        int index = 0;
        int w;
        int minRowWidth = 0;
        LinkedList<Cell> cells;
        for (Row r : rows) {
            index = 0;
            minRowWidth = 0;
            cells = r.getCells();
            for (Cell c : cells) {
                w = c.getOffsetWidth();
                if (widths[index] != w) {
                    w = widths[index];
                    c.setWidth(w + "px");
                }
                minRowWidth += w;
                index++;
            }
            r.getElement().getStyle()
                    .setProperty("minWidth", minRowWidth + "px");
        }
    }

    public int getCalculatedContentTop() {
        if (!visibleScrollBarY) {
            return contentTop - verticalScrollPos;
        }
        return contentTop;
    }

    private void updateContentHeight(int totalRows) {
        int contentHeight = totalRows * getMeasuredRowHeight();
        content.setHeight(contentHeight + "px");
    }

    public HeaderContainer getHeaderContainer() {
        return headerContainer;
    }

    /**
     * Set a header container that can be used to get header related
     * information.
     * 
     * @param headerContainer
     */
    public void setHeaderContainer(HeaderContainer headerContainer) {
        this.headerContainer = headerContainer;
        rowContainer.setHeaderContainer(headerContainer);
    }

    /**
     * Set a related object that needs to be updated during horizontal
     * scrolling.
     * 
     * @param relatedHorizontalScrollable
     */
    public void setRelatedHorizontalScrollable(
            Scrollable relatedHorizontalScrollable) {
        this.relatedHorizontalScrollable = relatedHorizontalScrollable;
    }

    public int getContentTop() {
        return contentTop;
    }

    public void setContentTop(int top) {
        contentTop = top;
        setInternalContentTop();
    }

    /**
     * Update content top if vertical scroll bar is not visible.
     */
    private void updateContentTop() {
        if (visibleScrollBarY) {
            return;
        }

        setInternalContentTop();
    }

    private void setInternalContentTop() {
        // Update content tops
        contentForRows.getElement().getStyle()
                .setTop(getCalculatedContentTop(), Unit.PX);
    }

    private void updateRowContentTop() {
        // Calculate row layout's top offset
        int scrollTop = getScrollTop();
        contentTop = scrollTop - (scrollTop % rowContainer.getRowHeight());
        // Notice buffer size
        int bufferHeight = scrollHandlerWidget.getBufferSize()
                * rowContainer.getRowHeight();
        if ((contentTop - bufferHeight) < 0) {
            contentTop = 0;
        } else {
            contentTop -= bufferHeight;
        }
    }

    public void resetVerticalScrollPosition() {
        scrollPanel.setVerticalScrollPosition(verticalScrollPos);
    }

    @Override
    public void setScrollTop(int verticalScrollPosition) {
        verticalScrollPos = verticalScrollPosition;
        if (visibleScrollBarY) {
            scrollPanel.setVerticalScrollPosition(verticalScrollPosition);
        }
    }

    @Override
    public void setScrollTop(int verticalScrollPosition, boolean silentScroll) {
        nextScrollEventIsSilent = silentScroll;
        verticalScrollPos = verticalScrollPosition;
        updateContentTop();
        if (!visibleScrollBarY) {
            scroll(silentScroll, getScrollLeft(), getScrollTop(), false);
        } else {
            // Launches a scroll event, which call scroll(...)
            scrollPanel.setVerticalScrollPosition(verticalScrollPosition);
        }
    }

    @Override
    public void setScrollLeft(int horizontalScrollPosition) {
        nextScrollEventIsSilent = true;
        scrollPanel.setHorizontalScrollPosition(horizontalScrollPosition);
    }

    /** Returns true, if this content panel shouldn't have vertical scroll bar. */
    public boolean isVisibleScrollBarY() {
        return visibleScrollBarY;
    }

    /*
     * Launches a scroll event. See VCustomScrollTable.scrollContent(...)
     * JavaDoc for variable descriptions.
     */
    private void scroll(boolean silent, int horScrollPos, int verScrollPos,
            boolean forceReset) {
        scrollHandlerWidget.scrollContent(this, horScrollPos, verScrollPos,
                forceReset, silent);
    }

    public void scrollVertically(int yAxisDelta, boolean byContentTop,
            boolean forceReset) {
        if (scrollPanel == null
                || !scrollHandlerWidget.isVerticalScrollbarVisible()) {
            return;
        }

        int p = getScrollTop();
        int max = getMaxScrollPosition();
        p += yAxisDelta;
        if (p < 0) {
            p = 0;
        } else if (p > max) {
            p = max;
        }
        verticalScrollPos = p;

        if (byContentTop) {
            updateContentTop();
            scroll(false, getScrollLeft(), getScrollTop(), forceReset);
        } else {
            scrollPanel.setVerticalScrollPosition(verticalScrollPos);
        }

    }

    public int getMaxScrollPosition() {
        int sh = scrollPanel.getElement().getScrollHeight();
        boolean hScrollbarVisible = scrollHandlerWidget
                .isHorizontalScrollbarVisible();
        int sph = scrollPanel.getOffsetHeight()
                - (hScrollbarVisible ? Util.getNativeScrollbarSize() : 0);
        return sh - sph;
    }

    @Override
    public boolean isHorizontalScrollbarVisible() {
        int w = scrollPanel.getElement().getPropertyInt("clientWidth");
        int cW = content.getElement().getPropertyInt("clientWidth");
        boolean horScrollbarVisible = w < cW;
        return horScrollbarVisible;
    }

    @Override
    public boolean isVerticalScrollbarVisible() {
        int h = scrollPanel.getElement().getPropertyInt("clientHeight");
        boolean verScrollbarVisible = h < content.getElement()
                .getOffsetHeight();
        return verScrollbarVisible;
    }

    @Override
    public int getScrollLeft() {
        return scrollPanel.getHorizontalScrollPosition();
    }

    @Override
    public int getScrollTop() {
        int p = 0;
        if (!visibleScrollBarY) {
            p = verticalScrollPos;
        } else {
            p = scrollPanel.getVerticalScrollPosition();
        }

        int max = getMaxScrollPosition();
        if (p < 0) {
            p = 0;
        } else if (p > max) {
            p = max;
        }
        return p;
    }

    @Override
    public Scrollable getRelatedScrollable() {
        return relatedHorizontalScrollable;
    }

    public void setHeight(int contentHeight) {
        setHeight(contentHeight + "px");
        scrollPanel.setHeight(contentHeight + "px");
    }

}
