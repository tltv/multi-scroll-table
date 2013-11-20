package org.vaadin.tltv.multiscrolltable.demo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.tltv.multiscrolltable.ui.Column;
import org.vaadin.tltv.multiscrolltable.ui.ColumnGroup;
import org.vaadin.tltv.multiscrolltable.ui.CustomScrollTable;
import org.vaadin.tltv.multiscrolltable.ui.ScrollContent;

import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;

public class CustomScrollTableTest {

    Mockery mockery = new JUnit4Mockery();

    PaintTarget paintTarget;

    private CustomScrollTable table;

    String prop1 = "test prop 1";
    String prop2 = "test prop 2";

    @Before
    public void setUp() {
        paintTarget = mockery.mock(PaintTarget.class);

        table = new CustomScrollTable();
    }

    @After
    public void tearDown() {
        mockery.assertIsSatisfied();
    }

    private void addTestPropertiesAndVisibleColumns() {
        table.getContainerDataSource().addContainerProperty(prop1,
                String.class, null);
        table.getContainerDataSource().addContainerProperty(prop2,
                String.class, null);
        Collection<Object> visibleCols = new ArrayList<Object>();
        visibleCols.add(prop1);
        visibleCols.add(prop2);
        table.setVisibleColumns(visibleCols);
    }

    private void addTestItems() {
        table.getContainerDataSource().addItem("1");
        table.getContainerDataSource().addItem("2");
    }

    @Test
    public void testSetVisibleColumns_Empty() {
        Collection<Object> visibleCols = new ArrayList<Object>();
        table.setVisibleColumns(visibleCols);
        assertArrayEquals(visibleCols.toArray(), table.getVisibleColumns());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVisibleColumns_NotEmpty_Fail() {
        Collection<Object> visibleCols = new ArrayList<Object>();
        visibleCols.add(new Object());
        visibleCols.add(new Object());
        table.setVisibleColumns(visibleCols);
        assertArrayEquals(visibleCols.toArray(), table.getVisibleColumns());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVisibleColumns_DoesntExist_Fail() {
        Collection<Object> visibleCols = new ArrayList<Object>();
        visibleCols.add(prop1);
        visibleCols.add(prop2);
        table.setVisibleColumns(visibleCols);
        assertArrayEquals(visibleCols.toArray(), table.getVisibleColumns());
    }

    @Test
    public void testSetVisibleColumns_NotEmpty_Exist() {
        table.getContainerDataSource().addContainerProperty(prop1,
                String.class, null);
        table.getContainerDataSource().addContainerProperty(prop2,
                String.class, null);
        Collection<Object> visibleCols = new ArrayList<Object>();
        visibleCols.add(prop1);
        visibleCols.add(prop2);
        table.setVisibleColumns(visibleCols);
        assertArrayEquals(visibleCols.toArray(), table.getVisibleColumns());
    }

    @Test
    public void testAddNewScrollContent() {
        assertTrue(table.getScrollContents().size() == 0);

        addTestPropertiesAndVisibleColumns();

        ScrollContent sc = new ScrollContent();
        ColumnGroup cg = new ColumnGroup();
        cg.setCaption("Column group caption");
        cg.addColumn(new Column(prop1));
        cg.addColumn(new Column(prop2));
        sc.addColumnGroup(cg);
        table.addScrollContent(sc);

        assertTrue(table.getScrollContents().size() == 1);
        ScrollContent scrollcontent = table.getScrollContents().iterator()
                .next();
        assertTrue(scrollcontent.equals(sc));
        assertTrue(scrollcontent.getColumns().size() == 2);
        assertTrue(scrollcontent.getColumnGroups().size() == 1);

        table.addScrollContent(sc); // Try to add again the same scroll content
        assertTrue(table.getScrollContents().size() == 1);
    }

    @Test
    public void testRemoveScrollContent() {
        addTestPropertiesAndVisibleColumns();

        ScrollContent sc = new ScrollContent();
        ColumnGroup cg = new ColumnGroup();
        cg.setCaption("Column group caption");
        cg.addColumn(new Column(prop1));
        cg.addColumn(new Column(prop2));
        sc.addColumnGroup(cg);
        table.addScrollContent(sc);
        table.removeScrollContent(sc);

        assertTrue(table.getScrollContents().size() == 0);
    }

    @Test
    public void testPaintContent() throws PaintException {
        addTestPropertiesAndVisibleColumns();

        mockery.checking(new Expectations() {
            {
                ignoring(paintTarget);
            }
        });
        assertTrue(isMeasureSpaceForRowsAvailable());
        assertTrue(isRowStructureChanged());
        assertTrue(getPageBuffer() == null);
        assertEquals(0, table.size());

        table.paintContent(paintTarget); // First paint
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(isRowStructureChanged());
        assertTrue(getPageBuffer() != null);
        assertEquals(0, table.size());

        table.paintContent(paintTarget); // Second paint
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(!isRowStructureChanged());
        assertTrue(getPageBuffer() != null);
        assertEquals(0, table.size());
    }

    @Test
    public void testAddNewItems() throws PaintException {
        addTestPropertiesAndVisibleColumns();
        mockery.checking(new Expectations() {
            {
                ignoring(paintTarget);
            }
        });
        table.paintContent(paintTarget); // First paint
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(isRowStructureChanged());
        assertTrue(getPageBuffer() != null);
        assertEquals(0, table.size());

        addTestItems();
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(isRowStructureChanged());
        assertTrue(getPageBuffer() == null);
        assertEquals(2, table.size());

        table.paintContent(paintTarget); // Second paint, first with rows
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(!isRowStructureChanged());
        assertTrue(getPageBuffer() != null);
        assertEquals(2, table.size());
    }

    @Test
    public void testRemoveNewItems() throws PaintException {
        addTestPropertiesAndVisibleColumns();
        addTestItems();
        mockery.checking(new Expectations() {
            {
                ignoring(paintTarget);
            }
        });
        table.paintContent(paintTarget); // First paint
        table.paintContent(paintTarget);// Second paint
        assertTrue(!isMeasureSpaceForRowsAvailable());
        assertTrue(!isRowStructureChanged());
        assertTrue(getPageBuffer() != null);

        table.getContainerDataSource().removeItem("1");
        assertTrue(isRowStructureChanged());
        assertTrue(getPageBuffer() == null);
        assertEquals(1, table.size());

        table.paintContent(paintTarget); // Third paint
        assertTrue(!isRowStructureChanged());
        assertTrue(getPageBuffer() != null);
        assertEquals(1, table.size());
    }

    private boolean isMeasureSpaceForRowsAvailable() {
        return (Boolean) ObjectUtils.getFieldValue(table,
                "measureSpaceForRowsAvailable");
    }

    private boolean isRowStructureChanged() {
        return (Boolean) ObjectUtils
                .getFieldValue(table, "rowStructureChanged");
    }

    private Object getPageBuffer() {
        return ObjectUtils.getFieldValue(table, "pageBuffer");
    }
}
