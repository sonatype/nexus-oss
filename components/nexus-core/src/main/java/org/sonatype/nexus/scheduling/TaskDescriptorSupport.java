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
package org.sonatype.nexus.scheduling;

import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.formfields.FormField;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support class for {@link TaskDescriptor}s.
 *
 * @since 3.0
 */
public abstract class TaskDescriptorSupport<T extends Task>
    implements TaskDescriptor<T>
{
  private final Class<T> type;

  private final String name;

  public TaskDescriptorSupport(final Class<T> type, final String name) {
    this.type = checkNotNull(type);
    this.name = checkNotNull(name);
  }

  @Override
  public final String getId() {
    return type.getName();
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final Class<T> getType() {
    return type;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public boolean isExposed() {
    return true;
  }

  @Override
  public List<FormField> formFields() {
    return Collections.emptyList();
  }
}
