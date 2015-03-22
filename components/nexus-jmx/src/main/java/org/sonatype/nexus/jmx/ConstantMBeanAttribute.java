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
package org.sonatype.nexus.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Constant-value {@link MBeanAttribute}.
 *
 * @since 3.0
 */
public class ConstantMBeanAttribute
  extends ComponentSupport
  implements MBeanAttribute
{
  private final MBeanAttributeInfo info;

  private final String name;

  private final Object value;

  public ConstantMBeanAttribute(final MBeanAttributeInfo info,
                                final Object value)
  {
    this.info = checkNotNull(info);
    this.name = info.getName();
    this.value = value;
  }

  @Override
  public MBeanAttributeInfo getInfo() {
    return info;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(final Object value) throws Exception {
    throw new AttributeNotFoundException("Attribute is constant: " + name);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "name='" + name + '\'' +
        ", value=" + value +
        '}';
  }

  //
  // Builder
  //

  /**
   * {@link ConstantMBeanAttribute} builder.
   */
  public static class Builder
    extends ComponentSupport
  {
    private String name;

    private String description;

    private Object value;

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder value(final Object value) {
      this.value = value;
      return this;
    }

    public ConstantMBeanAttribute build() {
      checkState(name != null);
      checkState(value != null);

      MBeanAttributeInfo info = new MBeanAttributeInfo(
          name,
          value.getClass().getName(),
          description,
          true, // readable
          false, // writable
          false, // is-form
          null // descriptor
      );

      log.trace("Building attribute with info: {}", info);
      return new ConstantMBeanAttribute(info, value);
    }
  }
}
