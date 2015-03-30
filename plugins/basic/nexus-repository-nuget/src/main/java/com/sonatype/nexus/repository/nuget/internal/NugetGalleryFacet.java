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

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Payload;

import org.joda.time.DateTime;

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
   * Add or update a package to the nuget gallery.
   */
  void put(InputStream inputStream) throws IOException, NugetPackageException;

  /**
   * Insert metadata a package into the gallery.
   */
  void putMetadata(final Map<String, String> metadata);

  /**
   * Attach content to pre-existing metadata.
   */
  void putContent(String id, String version, InputStream content);

  /**
   * Get a package, or {@code null} if not found.
   */
  @Nullable
  Payload get(String id, String version) throws IOException;

  /**
   * Gets the last updated date of a package, or {@code null} if not found.
   */
  DateTime getLastUpdatedDate(String id, String version);

  /**
   * Delete a package and return whether it existed.
   */
  boolean delete(String id, String version) throws IOException;

  /**
   * Returns named feed of packages matching the given query.
   *
   * @param base       Base URI
   * @param operation  Feed name
   * @param parameters OData query parameters
   * @return NuGet feed XML
   */
  public String feed(final String base, final String operation, final Map<String, String> parameters);

  /**
   * Returns entry XML for a given package ID and version.
   *
   * @param base    Base URI
   * @param id      package id
   * @param version package version
   */
  public String entry(final String base, final String id, final String version);

  /**
   * Returns the number of matching packages
   *
   * @param operation  typically a feed name followed by "/$count"
   * @param parameters OData query parameters
   */
  public int count(final String operation, final Map<String, String> parameters);
}
