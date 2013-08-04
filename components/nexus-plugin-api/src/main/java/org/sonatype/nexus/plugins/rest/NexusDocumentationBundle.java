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

package org.sonatype.nexus.plugins.rest;

/**
 * A special resource bundle that holds static (preferably static HTML) documentation.
 *
 * @author velo
 * @author cstamas
 */
public interface NexusDocumentationBundle
    extends NexusResourceBundle
{
  /**
   * Returns the plugin ID (artifactId?) of the plugin contaning this resource. This string should obey all rules
   * that
   * are prescribed for Maven3 artifactId validation. It makes the very 1st segment of the documentation URIs.
   */
  String getPluginId();

  /**
   * Returns the "url snippet". It makes possible to do a deeper "partition" within plugin documentation URIs. Used
   * by
   * plugins that may carry multiple documentations (like core doc plugin is). Others should just use defaults
   * (provided in {@link AbstractDocumentationNexusResourceBundle}.
   */
  String getPathPrefix();

  /**
   * Returns human description of the documentation bundle. Used for human consumption only: concise and short
   * description.
   */
  String getDescription();
}
