/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.maven.version;

/**
 * Thrown when a version or version range could not be parsed.
 * <p>
 * Note: sources copied from Aether release 1.9 for Nexus internal uses. Once Maven support is moved out from a core to
 * a plugin, this class should be removed and switch to Aether Version classes is to be done.
 * 
 * @author Benjamin Bentmann
 */
public class InvalidVersionSpecificationException
    extends Exception
{
    private final String version;

    public InvalidVersionSpecificationException( String version, String message )
    {
        super( message );
        this.version = version;
    }

    public InvalidVersionSpecificationException( String version, Throwable cause )
    {
        super( "Could not parse version specification " + version + getMessage( ": ", cause ), cause );
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public static String getMessage( String prefix, Throwable cause )
    {
        String msg = "";
        if ( cause != null )
        {
            msg = cause.getMessage();
            if ( msg == null || msg.length() <= 0 )
            {
                msg = cause.getClass().getSimpleName();
            }
            msg = prefix + msg;
        }
        return msg;
    }
}
