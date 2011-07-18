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
package org.sonatype.nexus.rt.boot;

import java.io.File;

import org.sonatype.appbooter.AbstractPlexusAppBooterCustomizer;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.appcontext.AppContext;
import org.sonatype.nexus.rt.prefs.FilePreferencesFactory;

public class ITAppBooterCustomizer
    extends AbstractPlexusAppBooterCustomizer
{
    public static final String TEST_ID_PREFIX = "testId=";

    @Override
    public void customizeContext( final PlexusAppBooter appBooter, final AppContext ctx )
    {
        customizeMavenIndexerBlockingCommits( appBooter, ctx );
        
        customizeJavaPrefs( appBooter, ctx );
    }
    
    protected void customizeMavenIndexerBlockingCommits(final PlexusAppBooter appBooter, final AppContext ctx)
    {
        // Note: in ITs we want to make Indexer perform blocking commits.
        // Since MavenIndexer 4.0, it performs async commits by default, meaning that no "helper" from Nexus
        // is able to tell and potentially block (see EventInspectorsUtil#waitForCalmPeriod() as example) execution
        // up to the moment when readers are refreshed (indexing operation IS done, but readers will not "see" the
        // change without reopening those).
        // By having this switch, we are switching Maven Indexer back into "blocking" mode as it was before 4.0.
        // The proper fix is to make all Indexer related ITs behave "properly" (with some heuristics?), and have some
        // sort of "try-wait-try-failAfterSomeRetries" the search operation itself.
        System.setProperty( "mavenIndexerBlockingCommits", Boolean.TRUE.toString() );
    }
    
    protected void customizeJavaPrefs(final PlexusAppBooter appBooter, final AppContext ctx)
    {
        String testId = getTestIdFromCmdLine( appBooter.getCommandLineArguments() );

        FilePreferencesFactory.setPreferencesFile( getFilePrefsFile( ctx.getBasedir(), testId ) );
    }

    protected String getTestIdFromCmdLine( String[] args )
    {
        // rather simple "parsing" of cmdLine
        for ( String arg : args )
        {
            if ( arg.startsWith( TEST_ID_PREFIX ) )
            {
                return arg.substring( TEST_ID_PREFIX.length() );
            }
        }

        return "unknown";
    }

    // ==

    public static File getFilePrefsFile( File dir, String testId )
    {
        return new File( dir, getFilePrefsFileName( testId ) );
    }

    public static String getFilePrefsFileName( String testId )
    {
        return testId + "-filePrefs";
    }

}
