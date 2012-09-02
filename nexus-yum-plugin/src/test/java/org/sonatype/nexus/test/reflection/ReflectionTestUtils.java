package org.sonatype.nexus.test.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionTestUtils {
	public static void setField(Object obj, String fieldName, Object value) {
		Class<?> clazz = obj.getClass();
		while (!Object.class.equals(clazz)) {
			try {
				Field decField = clazz.getDeclaredField(fieldName);
				decField.setAccessible(true);
				decField.set(obj, value);
				return;
			} catch (Exception e) {
			}
			clazz = clazz.getSuperclass();
		}

		throw new NoSuchFieldError(fieldName);
	}

	public static Method findMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		while (!Object.class.equals(clazz)) {
			try {
				Method method = clazz.getDeclaredMethod(name, parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				clazz = clazz.getSuperclass();
			}
		}

		throw new NoSuchMethodError(name);
	}
}
