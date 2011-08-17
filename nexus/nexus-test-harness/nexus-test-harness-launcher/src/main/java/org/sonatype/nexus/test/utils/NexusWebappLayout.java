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
 * @since 1.9.3
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
