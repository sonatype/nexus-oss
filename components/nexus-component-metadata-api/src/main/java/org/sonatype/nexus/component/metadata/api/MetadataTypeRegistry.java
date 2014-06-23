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

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MetadataTypeRegistry
    implements Iterable<MetadataType>
{
  protected final Map<String, MetadataType> byName = new HashMap<>();

  protected final Map<Class, MetadataType> byMetadataClass = new HashMap<>();

  public void registerType(MetadataType type) {
    checkNotNull(type);
    bindType(type);
    configureType(type);
  }

  @SuppressWarnings("unchecked")
  public <T> MetadataType<T> getType(Class<T> metadataClass) {
    checkNotNull(metadataClass);
    MetadataType type = byMetadataClass.get(metadataClass);
    if (type == null) {
      throw new IllegalArgumentException(metadataClass + " is not associated with a registered type");
    }
    return type;
  }

  @Override
  public Iterator<MetadataType> iterator() {
    return byMetadataClass.values().iterator();
  }

  private void bindType(MetadataType type) {
    if (byName.containsKey(type.getName())) {
      Class metadataClass = byName.get(type.getName()).getMetadataClass();
      throw new IllegalStateException("'" + type.getName() + "' is already bound to " + metadataClass);
    }
    if (byMetadataClass.containsKey(type.getMetadataClass())) {
      String name = byMetadataClass.get(type.getMetadataClass()).getName();
      throw new IllegalStateException(type.getMetadataClass() + " is already bound to '" + name + "'");
    }
    byName.put(type.getName(), type);
    byMetadataClass.put(type.getMetadataClass(), type);
  }

  protected abstract void configureType(MetadataType type);
}
