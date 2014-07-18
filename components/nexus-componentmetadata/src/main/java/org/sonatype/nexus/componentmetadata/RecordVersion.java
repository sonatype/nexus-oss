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
package org.sonatype.nexus.componentmetadata;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Version of a stored record.
 *
 * @since 3.0
 */
public final class RecordVersion
{
  private final String value;

  public RecordVersion(String value) {
    this.value = checkNotNull(value);
  }

  /**
   * Gets the value as a string.
   */
  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!(obj instanceof RecordVersion)) return false;
    final RecordVersion other = (RecordVersion) obj;
    return Objects.equal(getValue(), other.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getValue());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(RecordVersion.class)
        .add("value", getValue())
        .toString();
  }
}
