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
package org.sonatype.nexus.security.privilege;

import java.util.List;

import org.sonatype.nexus.security.config.CPrivilege;
import org.sonatype.nexus.security.config.SecurityConfigurationValidationContext;
import org.sonatype.nexus.validation.ValidationResponse;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

// NOTE: Not using existing AbstractPrivilegeDescriptor to investigate divorcing from legacy

/**
 * Support for {@link PrivilegeDescriptor} implementations.
 *
 * @since 3.0
 */
public abstract class PrivilegeDescriptorSupport
  implements PrivilegeDescriptor
{
  public static final String ALL = "*";

  private final String type;

  public PrivilegeDescriptorSupport(final String type) {
    this.type = checkNotNull(type);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        '}';
  }

  @Override
  public ValidationResponse validatePrivilege(final CPrivilege privilege,
                                              final SecurityConfigurationValidationContext context,
                                              final boolean update)
  {
    // FIXME: For now ignore validation
    return new ValidationResponse();
  }

  /**
   * Helper to read a privilege property and return default-value if unset or empty.
   */
  protected String readProperty(final CPrivilege privilege, final String name, final String defaultValue) {
    String value = privilege.getProperty(name);
    if (Strings.nullToEmpty(value).isEmpty()) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Helper to read a privilege property and parse out list.
   */
  protected List<String> readListProperty(final CPrivilege privilege, final String name, final String defaultValue) {
    String value = readProperty(privilege, name, defaultValue);
    return Lists.newArrayList(Splitter.on(',').omitEmptyStrings().trimResults().split(value));
  }
}
