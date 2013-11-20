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

import org.vaadin.tltv.multiscrolltable.client.MultiScrollTableServerRpc;
import org.vaadin.tltv.multiscrolltable.client.MultiScrollTableState;
import org.vaadin.tltv.multiscrolltable.client.event.MultiScrollTableEventHandler;
import org.vaadin.tltv.multiscrolltable.ui.CustomScrollTable;

import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.UIDL;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.shared.ui.Connect;

@Connect(CustomScrollTable.class)
public class MultiScrollTableConnector extends LegacyConnector implements
        SimpleManagedLayout {

    @Override
    protected void init() {
        super.init();
        getWidget().setEventHandler(eventHandler);
    }

    @Override
    public VCustomScrollTable getWidget() {
        return (VCustomScrollTable) super.getWidget();
    }

    @Override
    public MultiScrollTableState getState() {
        return (MultiScrollTableState) super.getState();
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        getWidget().updateFromUIDL(uidl, client);
        getWidget().onStateChanged();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
    }

    @Override
    public void layout() {
        int newHeight = getLayoutManager().getOuterHeight(
                getWidget().getElement());
        if (newHeight != getWidget().getIntHeight()) {
            getWidget().setIntHeight(newHeight);
            boolean isHeightUndefined = getWidget().getIntHeight() == -1;

            if (!isHeightUndefined) {
                getWidget().setIntHeight(getWidget().getIntHeight());
            } else {
                getWidget().setIntHeight(-1);
            }

            getWidget().recalculateHeights();
        }

        int newWidth = getLayoutManager().getOuterWidth(
                getWidget().getElement());
        if (newWidth != getWidget().getIntWidth()) {
            getWidget().setIntWidth(newWidth);
            boolean isWidthUndefined = getWidget().getIntWidth() == -1;

            if (!isWidthUndefined) {
                getWidget().setIntWidth(getWidget().getIntWidth());
            } else {
                getWidget().setIntWidth(-1);
            }

            getWidget().recalculateWidths();
        }
    }

    private final MultiScrollTableEventHandler eventHandler = new MultiScrollTableEventHandler() {

        @Override
        public void onUpdateVisibleRowCount(Integer newVisibleRows) {
            getRpcProxy(MultiScrollTableServerRpc.class).updateVisibleRowCount(
                    newVisibleRows);
        }

        @Override
        public void onUpdateFirstRowIndexAndVisibleRowCount(
                Integer newFirstRowIndex, Integer newVisibleRows) {
            getRpcProxy(MultiScrollTableServerRpc.class)
                    .updateFirstRowIndexAndVisibleRowCount(newFirstRowIndex,
                            newVisibleRows);
        }

        @Override
        public void onUpdateFirstRowIndex(Integer newFirstRowIndex) {
            getRpcProxy(MultiScrollTableServerRpc.class).updateFirstRowIndex(
                    newFirstRowIndex);
        }
    };
}
