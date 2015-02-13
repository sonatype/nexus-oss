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
package com.sonatype.nexus.repository.nuget.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Parameters;

/**
 * Provides persistent storage for Nuget content.
 *
 * @since 3.0
 */
@Facet.Exposed
public interface NugetGalleryFacet
    extends Facet
{
  /**
   * Returns the count of packages matching the given query; scoped to the given repositories.
   *
   * @param path       Typically a feed name plus "/$count"
   * @param parameters containing the query elements
   * @return Package count
   */
  int count(final String path, final Parameters parameters);

  /**
   * Returns named feed of packages matching the given query; scoped to the given repositories.
   *
   * @param base  Base URI
   * @param name  Feed name
   * @param query OData query
   * @return NuGet feed
   */
  String feed(String base, String name, final Parameters query);

  /**
   * Returns the package entry for the given Id and Version; scoped to the given repositories.
   *
   * @param base    Base URI
   * @param id      Package Id
   * @param version Package Version
   * @return NuGet entry; {@code null} if not found
   */
  String entry(String base, String id, String version);

  /**
   * Locates the package with the same Id and Version; scoped to the given repositories.
   *
   * @param id      Package Id
   * @param version Package Version
   * @return Package location; {@code null} if unknown
   */
  String locate(String id, String version);

  /**
   * Identifies the given package and returns its Id and Version coordinates.
   *
   * @param location Package location
   * @return Id and Version coordinates; {@code null} if unknown
   */
  String[] identify(String location);

  /**
   * Add or update a package to the nuget gallery.
   */
  void put(InputStream inputStream) throws IOException, NugetPackageException;
}
