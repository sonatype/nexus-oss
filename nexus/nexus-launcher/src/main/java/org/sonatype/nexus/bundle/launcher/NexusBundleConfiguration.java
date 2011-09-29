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
package org.sonatype.nexus.bundle.launcher;

import org.sonatype.sisu.bl.BundleConfiguration;

import java.io.File;
import java.util.List;

/**
 * An Nexus bundle configuration.
 *
 * @since 1.10.0
 */
public interface NexusBundleConfiguration
    extends BundleConfiguration<NexusBundleConfiguration>
{

    /**
     * Returns additional plugins to be installed in Nexus.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @return Nexus plugins to be installed
     * @since 1.10.0
     */
    List<File> getPlugins();

    /**
     * Sets plugins to be installed in Nexus. Provided plugins will overwrite existing configured plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed. Can be null, case when an empty list will be used
     * @return itself, for usage in fluent api
     * @since 1.10.0
     */
    NexusBundleConfiguration setPlugins( List<File> plugins );

    /**
     * Sets plugins to be installed in Nexus. Provided plugins will overwrite existing configured plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed
     * @return itself, for usage in fluent api
     * @since 1.10.0
     */
    NexusBundleConfiguration setPlugins( File... plugins );

    /**
     * Append plugins to existing set of plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed
     * @return itself, for usage in fluent api
     * @since 1.10.0
     */
    NexusBundleConfiguration addPlugins( File... plugins );

}
