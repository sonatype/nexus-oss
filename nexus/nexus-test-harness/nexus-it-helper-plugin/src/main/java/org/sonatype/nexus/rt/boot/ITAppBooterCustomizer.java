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
package org.sonatype.nexus.rt.boot;


public class ITAppBooterCustomizer
   // extends AbstractPlexusAppBooterCustomizer
{
    public static final String TEST_ID_PREFIX = "testId=";
/* JUST LEFT HERE FOR FUTURE REFERENCE
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
*/
}
