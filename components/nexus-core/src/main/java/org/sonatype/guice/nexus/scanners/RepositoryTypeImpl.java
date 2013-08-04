/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.guice.nexus.scanners;

import java.lang.annotation.Annotation;

import org.sonatype.nexus.plugins.RepositoryType;

/**
 * Runtime implementation of Nexus @{@link RepositoryType} annotation.
 */
final class RepositoryTypeImpl
    implements RepositoryType
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final String pathPrefix;

  private final int repositoryMaxInstanceCount;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  RepositoryTypeImpl(final String pathPrefix, final int repositoryMaxInstanceCount) {
    if (null == pathPrefix) {
      throw new IllegalArgumentException("@RepositoryType cannot contain null values");
    }

    this.pathPrefix = pathPrefix;
    this.repositoryMaxInstanceCount = repositoryMaxInstanceCount;
  }

  // ----------------------------------------------------------------------
  // Annotation properties
  // ----------------------------------------------------------------------

  public String pathPrefix() {
    return pathPrefix;
  }

  public int repositoryMaxInstanceCount() {
    return repositoryMaxInstanceCount;
  }

  // ----------------------------------------------------------------------
  // Standard annotation behaviour
  // ----------------------------------------------------------------------

  @Override
  public boolean equals(final Object rhs) {
    if (this == rhs) {
      return true;
    }

    if (rhs instanceof RepositoryType) {
      final RepositoryType type = (RepositoryType) rhs;

      return pathPrefix.equals(type.pathPrefix())
          && repositoryMaxInstanceCount == type.repositoryMaxInstanceCount();
    }

    return false;
  }

  @Override
  public int hashCode() {
    return (127 * "pathPrefix".hashCode() ^ pathPrefix.hashCode())
        + (127 * "repositoryMaxInstanceCount".hashCode() ^ Integer.valueOf(repositoryMaxInstanceCount).hashCode());
  }

  @Override
  public String toString() {
    return String.format("@%s(pathPrefix=%s, repositoryMaxInstanceCount=%s)", RepositoryType.class.getName(),
        pathPrefix, Integer.valueOf(repositoryMaxInstanceCount));
  }

  public Class<? extends Annotation> annotationType() {
    return RepositoryType.class;
  }
}