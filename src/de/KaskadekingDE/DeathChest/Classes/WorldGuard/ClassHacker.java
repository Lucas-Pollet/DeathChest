package de.KaskadekingDE.DeathChest.Classes.WorldGuard;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ClassHacker
{
    public static void setStaticValue(Field field, Object value)
    {
        try
        {
            Field modifier = Field.class.getDeclaredField("modifiers");

            modifier.setAccessible(true);
            modifier.setInt(field, field.getModifiers() & 0xFFFFFFEF);
            field.set(null, value);
        } catch (Exception ex) {}
    }

    public static void setPrivateValue(Object obj, String name, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        }
        catch (IllegalArgumentException ex) {}catch (IllegalAccessException ex) {}catch (Exception ex) {}
    }





    public static Object getPrivateValue(Object obj, String name)
    {
        Field field = null;
        Class clazz = obj.getClass();
        try
        {
            do
            {
                field = clazz.getDeclaredField(name);
                clazz = clazz.getSuperclass();
            }
            while ((field == null) && (clazz != null));
            if (field == null)
            {
                return null;
            }
            field.setAccessible(true);
            return field.get(obj);
        }
        catch (Exception ex) {}

        return null;
    }


    public static Object callPrivateMethod(Object obj, String name, Class[] paramTypes, Object[] params)
    {
        Method method = null;
        Class clazz = obj.getClass();
        try
        {
            do
            {
                method = clazz.getDeclaredMethod(name, paramTypes);
                clazz = clazz.getSuperclass();
            }
            while ((method == null) && (clazz != null));
            if (method == null)
            {
                return null;
            }
            method.setAccessible(true);
            return method.invoke(obj, params);
        }
        catch (Exception ex) {}

        return null;
    }
}