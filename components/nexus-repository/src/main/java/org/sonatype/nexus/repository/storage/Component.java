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
package org.sonatype.nexus.repository.storage;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Metadata about a software component.
 *
 * @since 3.0
 */
public interface Component
    extends MetadataNode
{
  /**
   * Gets the group or {@code null} if undefined.
   */
  @Nullable
  String group();

  /**
   * Gets the group or throws a runtime exception if undefined.
   */
  String requireGroup();

  /**
   * Sets the group to the given value, or {@code null} to un-define it.
   */
  Component group(@Nullable String group);

  /**
   * Gets the (required) name or throws a runtime exception if undefined.
   */
  String name();

  /**
   * Sets the (required) name.
   */
  Component name(String name);

  /**
   * Gets the version or {@code null} if undefined.
   */
  @Nullable
  String version();

  /**
   * Gets the version or throws a runtime exception if undefined.
   */
  String requireVersion();

  /**
   * Sets the version to the given value, or {@code null} to un-define it.
   */
  Component version(@Nullable String version);

  /**
   * Gets the assets that belong to this component, possibly empty, never {@code null}.
   */
  List<Asset> assets();

  /**
   * Gets the first asset that belongs to this component or throws a runtime exception if undefined.
   */
  Asset firstAsset();
}
