package com.ruoyi.parking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

final class ParkingTestBeanProperties
{
    private ParkingTestBeanProperties()
    {
    }

    static void assertHasProperty(Class<?> beanType, String propertyName)
    {
        assertTrue(
            hasProperty(beanType, propertyName),
            () -> "Expected property '" + propertyName + "' on " + beanType.getSimpleName()
        );
    }

    static void assertNoProperty(Class<?> beanType, String propertyName)
    {
        assertFalse(
            hasProperty(beanType, propertyName),
            () -> "Did not expect property '" + propertyName + "' on " + beanType.getSimpleName()
        );
    }

    static void setLongProperty(Object target, String propertyName, Long value)
    {
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        assertTrue(
            wrapper.isWritableProperty(propertyName),
            () -> "Expected writable property '" + propertyName + "' on " + target.getClass().getSimpleName()
        );
        wrapper.setPropertyValue(propertyName, value);
    }

    static Long getLongProperty(Object target, String propertyName)
    {
        BeanWrapper wrapper = new BeanWrapperImpl(target);
        assertTrue(
            wrapper.isReadableProperty(propertyName),
            () -> "Expected readable property '" + propertyName + "' on " + target.getClass().getSimpleName()
        );
        Object value = wrapper.getPropertyValue(propertyName);
        return value == null ? null : ((Number) value).longValue();
    }

    private static boolean hasProperty(Class<?> beanType, String propertyName)
    {
        return Arrays.stream(new BeanWrapperImpl(beanType).getPropertyDescriptors())
            .map(PropertyDescriptor::getName)
            .anyMatch(propertyName::equals);
    }
}
