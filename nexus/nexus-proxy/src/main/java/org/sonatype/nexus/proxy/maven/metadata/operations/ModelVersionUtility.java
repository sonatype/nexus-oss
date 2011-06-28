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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;

/**
 * Handling model version of Maven repository metadata, with some rudimentary "version detection".
 * 
 * @author cstamas
 */
public class ModelVersionUtility
{
    public static final Version LATEST_MODEL_VERSION = Version.values()[Version.values().length - 1];

    public enum Version
    {
        V100,

        V110;
    }

    public static Version getModelVersion( final Metadata metadata )
    {
        if ( "1.1.0".equals( metadata.getModelVersion() ) )
        {
            return Version.V110;
        }
        else
        {
            return Version.V100;
        }
    }

    public static void setModelVersion( final Metadata metadata, final Version version )
    {
        switch ( version )
        {
            case V100:
                metadata.setModelVersion( null );
                Versioning versioning = metadata.getVersioning();
                if ( versioning != null )
                {
                    versioning.setSnapshotVersions( null );
                }
                break;

            case V110:
                metadata.setModelVersion( "1.1.0" );
                break;
        }
    }
}
