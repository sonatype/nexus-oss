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
package org.sonatype.nexus.test.utils;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;

/**
 * Provides general information about the Nexus Webapp.
 * <p>
 * Particularly useful in tests where static information about the Nexus webapp is needed.
 * <p>
 * Static members representing paths should not begin with slash. All path separators in String paths are
 * forward-slashes.
 *
 * @since 2.0
 * @author plynch
 */
public class NexusWebappLayout
{
    /**
     * Relative path to the properties file within the Nexus webapp that serves as the primary DI container and
     * application configuration file.
     * <p>
     * In releases up to 1.9.3, this was WEB-INF/plexus.properties
     */
    public static final String PATH_PLEXUS_PROPERTIES = "WEB-INF/plexus.properties";

    public static final String PATH_LOGBACK_XML = "WEB-INF/classes/logback.xml";

    private File webappFile;

    /**
     * @param webappPath the absolute or relative path to the extracted webapp from which all other paths exposed by
     *            this instance should be derived.
     * @throws NullPointerException if webappPath is null
     */
    public NexusWebappLayout( final String webappPath )
    {
        this( new File( webappPath ) );
    }

    /**
     * Construct info for the Nexus Webapp located at webappFile.
     *
     * @param webappFile a file representing the extracted webapp location.
     */
    public NexusWebappLayout( final File webappFile )
    {
        this.webappFile = webappFile;
    }

    /**
     * @return the webapp file of the Nexus webapp represented by this instance.
     */
    public File getWebappFile()
    {
        return webappFile;
    }

    /**
     * @return the path to Nexus properties file derived from a combination of {@link #getWebappFile()} and
     *         {@link #PATH_PLEXUS_PROPERTIES}
     */
    public File getNexusPropertiesFile()
    {
        return FileUtils.resolveFile( getWebappFile(), PATH_PLEXUS_PROPERTIES );
    }

}
