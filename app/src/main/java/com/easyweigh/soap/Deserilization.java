package com.easyweigh.soap;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ksoap2.serialization.SoapObject;

public class Deserilization {
    public ArrayList SoapDeserializeArray(Class<?> itemClass, SoapObject object) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < object.getPropertyCount(); i++) {
            try {
                arrayList.add(itemClass.getConstructor(new Class[]{SoapObject.class}).newInstance(new Object[]{(SoapObject) object.getProperty(i)}));
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
            } catch (NoSuchMethodException e5) {
                e5.printStackTrace();
            }
        }
        return arrayList;
    }

    public <T> void SoapDeserilize(T item, SoapObject object) {
        for (Field field : item.getClass().getDeclaredFields()) {
            try {
                Log.d(field.getName(), field.getType().getName());
                if (field.getType().getName() == String.class.getName()) {
                    field.set(item, object.getProperty(field.getName()).toString());
                } else if (field.getType().getName() == Integer.class.getName() || field.getType().getName() == Integer.TYPE.getName()) {
                    Log.d("3", "int");
                    field.set(item, Integer.valueOf(Integer.parseInt(object.getProperty(field.getName()).toString())));
                } else if (field.getType().getName() == Float.class.getName()) {
                    Log.d("4", "float");
                    field.set(item, Float.valueOf(Float.parseFloat(object.getProperty(field.getName()).toString())));
                } else if (field.getType().getName() == Double.class.getName()) {
                    field.set(item, Double.valueOf(Double.parseDouble(object.getProperty(field.getName()).toString())));
                } else if (field.getType().getName() == Boolean.class.getName()) {
                    field.set(item, Boolean.valueOf(Boolean.parseBoolean(object.getProperty(field.getName()).toString())));
                } else if (List.class.isAssignableFrom(field.getType())) {
                    Log.d("array", field.getGenericType().toString());
                    SoapObject fieldArray = (SoapObject) object.getProperty(field.getName());
                    Log.d("array", "1");
                    Class genericClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    Log.d("array", "2");
                    ArrayList list = new ArrayList();
                    Log.d("array", "2.2");
                    for (int i = 0; i < fieldArray.getPropertyCount(); i++) {
                        Log.d("array", "2.8");
                        Object newObject = genericClass.getConstructor(new Class[]{SoapObject.class}).newInstance(new Object[]{(SoapObject) fieldArray.getProperty(i)});
                        Log.d("array", "3");
                        list.add(newObject);
                        Log.d("array", "4");
                    }
                    field.set(item, list);
                } else {
                    Constructor constructor = field.getType().getConstructor(new Class[]{SoapObject.class});
                    Object[] objArr = new Object[1];
                    objArr[0] = (SoapObject) object.getProperty(field.getName());
                    field.set(item, constructor.newInstance(objArr));
                }
            } catch (Exception e) {
                Log.d("FieldNotFound:", new StringBuilder(StringUtils.SPACE).append(e.getMessage()).toString());
            }
        }
    }
}
