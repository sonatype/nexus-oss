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
package org.sonatype.nexus.component.model;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Identifies a stored component or asset.
 * 
 * @since 3.0
 */
public class EntityId
{
  private final String uniqueString;

  /**
   * Creates an instance using the given unique string.
   */
  public EntityId(String uniqueString) {
    this.uniqueString = checkNotNull(uniqueString);
  }

  /**
   * Gets the id as a unique string.
   */
  public String asUniqueString() {
    return uniqueString;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EntityId that = (EntityId) o;

    return uniqueString.equals(that.uniqueString);
  }

  @Override
  public int hashCode() {
    return uniqueString.hashCode();
  }

  @Override
  public String toString() {
    return EntityId.class.getSimpleName() + "[" + uniqueString + "]";
  }
}
