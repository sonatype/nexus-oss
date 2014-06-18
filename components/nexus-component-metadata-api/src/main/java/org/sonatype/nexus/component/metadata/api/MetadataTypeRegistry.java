/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.component.metadata.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MetadataTypeRegistry
    implements Iterable<MetadataType>
{
  protected final Map<String, MetadataType> byName = new HashMap<>();

  protected final Map<Class, MetadataType> byClass = new HashMap<>();

  public void registerType(MetadataType type) {
    checkNotNull(type);
    bindType(type);
    configureType(type);
  }

  public MetadataType getType(Object object) {
    MetadataType type = getType(object.getClass());
    if (type == null) {
      throw new IllegalArgumentException(object.getClass() + " is not associated with any registered MetadataType");
    }
    return type;
  }

  @Nullable
  public MetadataType getType(Class pojoClass) {
    checkNotNull(pojoClass);
    return byClass.get(pojoClass);
  }

  @Override
  public Iterator<MetadataType> iterator() {
    return byClass.values().iterator();
  }

  private void bindType(MetadataType type) {
    if (byName.containsKey(type.getName())) {
      Class boundClass = byName.get(type.getName()).getPojoClass();
      throw new IllegalStateException("Type name '" + type.getName() + "' is already bound to " + boundClass);
    }
    if (byClass.containsKey(type.getPojoClass())) {
      String boundName = byClass.get(type.getPojoClass()).getName();
      throw new IllegalStateException("Type class " + type.getPojoClass() + " is already bound to '" + boundName + "'");
    }
    byName.put(type.getName(), type);
    byClass.put(type.getPojoClass(), type);
  }

  protected abstract void configureType(MetadataType type);
}
