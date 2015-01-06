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


import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A unique identifier with a guaranteed-unique portion, and a user-friendly name. The user-friendly portion will
 * is intended to be unique in a given namespace, but it may re-appear if the named item is deleted/removed and
 * another one added with the same user-friendly name.
 *
 * Neither name nor identifier may contain the colon (':') character.
 *
 * @since 3.0
 */
public abstract class ComponentOriginId
    implements Serializable
{
  private final String name;

  private final String internalId;

  public ComponentOriginId(final String name, final String internalId) {
    validatePortions(name, internalId);

    this.name = name;
    this.internalId = internalId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ComponentOriginId)) {
      return false;
    }

    ComponentOriginId that = (ComponentOriginId) o;

    return Objects.equals(name, that.name) && Objects.equals(internalId, that.internalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, internalId);
  }

  @Override
  public String toString() {
    return com.google.common.base.Objects.toStringHelper(getClass()).add("name", name).add(
        "internalId", internalId).toString();
  }

  public String getName() {
    return name;
  }

  public String getInternalId() {
    return internalId;
  }

  private void validatePortions(final String name,
                                final String identifier)
  {
    // This is simpler, for now, than encoding colons, but maybe we'll want to do that.
    checkArgument(!checkNotNull(name).contains(":"), "name cannot contain the ':' character.");
    checkArgument(!checkNotNull(identifier).contains(":"), "internalId cannot contain ':' character.");
  }
}
