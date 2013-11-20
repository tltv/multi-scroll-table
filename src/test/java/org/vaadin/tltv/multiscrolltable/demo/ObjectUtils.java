package org.vaadin.tltv.multiscrolltable.demo;

import java.lang.reflect.Field;

public class ObjectUtils {

    /**
     * Gets the value of the field <code>fieldName</code> on the object
     * <code>o</code>. The field may be defined in either <code>o</code>'s class
     * or any of its super classes, and it need not have public visibility.
     * 
     * @param o
     *            the object on which the field value should be get.
     * @param fieldName
     *            the name of the field.
     * @throws IllegalArgumentException
     *             if the field value could not be get.
     * @return Field's value
     */
    public static Object getFieldValue(Object o, String fieldName)
            throws IllegalArgumentException {
        Object value = null;
        Field field = getField(o.getClass(), fieldName);
        try {
            boolean oldAccessible = field.isAccessible();
            field.setAccessible(true);
            try {
                value = field.get(o);
            } finally {
                field.setAccessible(oldAccessible);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get field value");
        }
        return value;
    }

    private static Field getField(final Class<?> clazz, final String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), fieldName);
            } else {
                return null;
            }
        }
    }
}
