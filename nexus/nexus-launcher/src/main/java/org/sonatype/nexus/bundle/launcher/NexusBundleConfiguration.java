/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher;

import org.sonatype.sisu.bl.BundleConfiguration;

import java.io.File;
import java.util.List;

/**
 * An Nexus bundle configuration.
 *
 * @since 2.0
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
     * @since 2.0
     */
    List<File> getPlugins();

    /**
     * Sets plugins to be installed in Nexus. Provided plugins will overwrite existing configured plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed. Can be null, case when an empty list will be used
     * @return itself, for usage in fluent api
     * @since 2.0
     */
    NexusBundleConfiguration setPlugins( List<File> plugins );

    /**
     * Sets plugins to be installed in Nexus. Provided plugins will overwrite existing configured plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed
     * @return itself, for usage in fluent api
     * @since 2.0
     */
    NexusBundleConfiguration setPlugins( File... plugins );

    /**
     * Append plugins to existing set of plugins.
     * <p/>
     * Plugins can be zips/jars/tars to be unpacked or directories to be copied
     *
     * @param plugins Nexus plugins to be installed
     * @return itself, for usage in fluent api
     * @since 2.0
     */
    NexusBundleConfiguration addPlugins( File... plugins );

}
