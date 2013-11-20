package org.vaadin.tltv.multiscrolltable.client;

import com.vaadin.shared.communication.ServerRpc;

public interface MultiScrollTableServerRpc extends ServerRpc {

    void updateFirstRowIndex(Integer newFirstRowIndex);

    void updateVisibleRowCount(Integer newVisibleRows);

    void updateFirstRowIndexAndVisibleRowCount(Integer newFirstRowIndex,
            Integer newVisibleRows);
}
