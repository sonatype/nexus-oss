/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.artifact;

import java.util.regex.Pattern;

/**
 * Utility methods for working with artifact version strings
 */
public class VersionUtils
{
	public static String SNAPSHOT_VERSION = "SNAPSHOT";
	
    private static NexusEnforcer enforcer = new DefaultNexusEnforcer();
    
    // Note that there is an 'OR' to support 2 different patterns.
    // i.e. the proper way 1.0-20080707.124343
    // i.e. the newly supported way 20080707.124343 (no base version, i.e. 1.0)
    private static final Pattern VERSION_FILE_PATTERN = 
        Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$|^([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    
    private static final Pattern STRICT_VERSION_FILE_PATTERN = 
        Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    
    public static boolean isSnapshot( String baseVersion )
    {
        if ( enforcer.isStrict() )
        {
            synchronized ( STRICT_VERSION_FILE_PATTERN )
            {
                return STRICT_VERSION_FILE_PATTERN.matcher( baseVersion ).matches()
                || baseVersion.endsWith( SNAPSHOT_VERSION );   
            }
        }
        else
        {
            synchronized ( VERSION_FILE_PATTERN )
            {
                return VERSION_FILE_PATTERN.matcher( baseVersion ).matches()
                || baseVersion.endsWith( SNAPSHOT_VERSION );   
            }
        }
    }
}
