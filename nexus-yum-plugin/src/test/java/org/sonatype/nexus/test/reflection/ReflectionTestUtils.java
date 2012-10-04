/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
