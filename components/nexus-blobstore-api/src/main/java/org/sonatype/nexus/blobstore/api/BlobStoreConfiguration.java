/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.api;

import java.util.Map;

import org.sonatype.nexus.common.entity.Entity;

/**
 * {@link BlobStore} configuration.
 *
 * @since 3.0
 */
public class BlobStoreConfiguration
  extends Entity
{
  private String name;
  
  private String recipeName;
  
  private Map<String, Map<String, Object>> attributes;
  
  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public void setRecipeName(final String recipeName) {
    this.recipeName = recipeName;
  }

  public Map<String, Map<String, Object>> getAttributes() {
    return attributes;
  }

  public void setAttributes(final Map<String, Map<String, Object>> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "name='" + name + '\'' +
        ", recipeName='" + recipeName + '\'' +
        ", attributes=" + attributes +
        '}';
  }
}
