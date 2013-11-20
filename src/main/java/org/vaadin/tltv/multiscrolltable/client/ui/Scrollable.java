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

public interface Scrollable {

    /**
     * Gets scroll left offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled right.
     * </p>
     * 
     * @return Horizontal scrolling position in pixels.
     */
    public int getScrollLeft();

    /**
     * Sets scroll left offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled right.
     * </p>
     * 
     * @param pixelsScrolled
     *            the xOffset.
     */
    public void setScrollLeft(int pixelsScrolled);

    /**
     * Gets scroll top offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled down.
     * </p>
     * 
     * @return Vertical scrolling position in pixels.
     */
    public int getScrollTop();

    /**
     * Sets scroll top offset.
     * <p>
     * Scrolling offset is the number of pixels this scrollable has been
     * scrolled down.
     * </p>
     * 
     * @param pixelsScrolled
     *            the yOffset.
     */
    public void setScrollTop(int pixelsScrolled);
}
