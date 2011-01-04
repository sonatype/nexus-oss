/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
     * Returns the plugin ID (artifactId?) of the plugin contaning this resource. This string should obey all rules that
     * are prescribed for Maven3 artifactId validation. It makes the very 1st segment of the documentation URIs.
     * 
     * @return
     */
    String getPluginId();

    /**
     * Returns the "url snippet". It makes possible to do a deeper "partition" within plugin documentation URIs. Used by
     * plugins that may carry multiple documentations (like core doc plugin is). Others should just use defaults
     * (provided in {@link AbstractDocumentationNexusResourceBundle}.
     * 
     * @return
     */
    String getPathPrefix();

    /**
     * Returns human description of the documentation bundle. Used for human consumption only: concise and short
     * description.
     * 
     * @return
     */
    String getDescription();
}
