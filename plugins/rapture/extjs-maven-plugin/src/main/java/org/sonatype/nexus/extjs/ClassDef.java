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
package org.sonatype.nexus.extjs;

import java.io.File;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * ExtJS class definition record.
 *
 * @since 3.0
 */
public class ClassDef
{
  private final String name;

  private final File source;

  private final Set<String> dependencies = Sets.newHashSet();

  public ClassDef(final String name, final File source) {
    this.name = name;
    this.source = source;
  }

  /**
   * The name of the ExtJS class.
   */
  public String getName() {
    return name;
  }

  /**
   * The file the ExtJS class is defined in.
   */
  public File getSource() {
    return source;
  }

  /**
   * A list of all ExtJS classes which this class depends on.
   */
  public Set<String> getDependencies() {
    return dependencies;
  }
}
