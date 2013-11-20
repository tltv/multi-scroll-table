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

/**
 * <b>Column</b> contains meta-data for a property in the CustomScrollTable's
 * data source container.
 */
public class Column {

    private Object columnId;

    private String caption;

    private ColumnGroup columnGroup;

    private boolean readonly;

    public Column() {
    }

    public Column(Object columnId) {
        this.columnId = columnId;
        setCaption(String.valueOf(columnId));
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Object getColumnId() {
        return columnId;
    }

    public void setColumnId(Object columnId) {
        this.columnId = columnId;
    }

    public ColumnGroup getColumnGroup() {
        return columnGroup;
    }

    public void setColumnGroup(ColumnGroup columnGroup) {
        this.columnGroup = columnGroup;
    }

}
