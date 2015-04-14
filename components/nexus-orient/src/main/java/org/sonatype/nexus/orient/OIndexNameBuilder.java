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
package org.sonatype.nexus.orient;

import java.util.Locale;

import com.orientechnologies.orient.core.index.OIndex;

import static com.google.common.base.Preconditions.checkState;

/**
 * Helper to build {@link OIndex} names.
 *
 * @since 3.0
 */
public class OIndexNameBuilder
{
  public static final String TYPE_SEPARATOR = ".";

  public static final String SUFFIX = "_idx";

  private String type;

  private String property;

  public OIndexNameBuilder type(final String type) {
    this.type = type;
    return this;
  }

  public OIndexNameBuilder property(final String property) {
    this.property = property;
    return this;
  }

  public String build() {
    // type is optional
    checkState(property != null, "Property required");

    StringBuilder buff = new StringBuilder();
    if (type != null) {
      buff.append(type);
      buff.append(TYPE_SEPARATOR);
    }
    buff.append(property);
    buff.append(SUFFIX);

    // OIndex names are always lower-case
    return buff.toString().toLowerCase(Locale.US);
  }
}
