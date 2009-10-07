/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.SelectorUtils;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleaner;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerRequest;
import org.sonatype.plugin.metadata.plexus.PlexusComponentGleanerResponse;

/**
 * Base class to be extended by Nexus plugins tests. Beside the standard {@link AbstractNexusTestCase} functionality
 * will scan additional paths for components, such as "target/classes", "target/test-classes", or ant-like classpath
 * entries.
 *
 * @author ...
 * @author Alin Dreghiciu
 */
public abstract class AbstractPluginTestCase
    extends AbstractNexusTestCase
{

    protected String[] sourceDirectories = { "target/classes", "target/test-classes" };

    protected void setupContainer()
    {
        super.setupContainer();

        try
        {
            // look through all the main and test classes, and create component descriptors if needed
            PlexusComponentGleaner plexusComponentGleaner = new PlexusComponentGleaner();
            List<String> projectClassNames = this.buildProjectClassList();

            for( String className : projectClassNames )
            {
                PlexusComponentGleanerRequest request = new PlexusComponentGleanerRequest( className, ClassLoader
                    .getSystemClassLoader()
                );

                PlexusComponentGleanerResponse gleanerResponse = plexusComponentGleaner.glean( request );

                if( gleanerResponse != null && gleanerResponse.getComponentDescriptor() != null )
                {
                    ComponentDescriptor<?> componentDescriptor = gleanerResponse.getComponentDescriptor();
//                    System.out.println( "... ... adding component role=\"" + componentDescriptor.getRole()
//                        + "\", hint=\"" + componentDescriptor.getRoleHint() + "\"" );
//                    System.out.println( new XStream().toXML( componentDescriptor ) );

                    this.getContainer().addComponentDescriptor( componentDescriptor );
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            fail( "Failed to glean classes" );
        }
    }

    private List<String> buildProjectClassList()
    {
        List<String> classNames = new ArrayList<String>();

        final String[] sourceDirectories = getSourceDirectories();
        if( sourceDirectories != null )
        {
            for( String sourceDir : sourceDirectories )
            {
                classNames.addAll( scanDirectory( new File( sourceDir ), "**/*.class" ) );
            }
        }
        final String[] classpathEntries = getClasspathEntries();
        if( classpathEntries != null && classpathEntries.length > 0 )
        {
            for( String classpathEntry : classpathEntries )
            {
                final String basePath = extractBasePattern( classpathEntry );
                final String pattern = classpathEntry.equals( basePath )
                                       ? "**/*.class"
                                       : classpathEntry.substring( basePath.length() + 1 );

                try
                {
                    final Enumeration<URL> urls = getClassLoader().getResources( basePath );
                    while( urls.hasMoreElements() )
                    {
                        final URL url = urls.nextElement();
                        if( "file".equals( url.getProtocol() ) )
                        {
                            final String rootPath = new File( url.toURI() ).getAbsolutePath();
                            final String root = rootPath.substring( 0, rootPath.lastIndexOf( basePath ) );
                            classNames.addAll( scanDirectory( new File( root ), basePath + "/" + pattern ) );
                        }
                        else if( "jar".equals( url.getProtocol() ) )
                        {
                            classNames.addAll( scanJar( url, pattern ) );
                        }
                    }
                }
                catch( IOException ignore )
                {
                    // ignore
                }
                catch( URISyntaxException ignore )
                {
                    // ignore
                }
            }
        }
        return classNames;
    }

    /**
     * Scans a file system directory for classes matching an ant-style path mapping.
     *
     * @param dir     directory to be scanned
     * @param pattern ant-style pattern
     *
     * @return list of matching class names
     */
    private static List<String> scanDirectory( final File dir,
                                               final String pattern )
    {
        final List<String> classNames = new ArrayList<String>();
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( dir );
        scanner.addDefaultExcludes();
        scanner.setIncludes( new String[]{ pattern } );

        scanner.scan();

        for( String fileNameOfClass : scanner.getIncludedFiles() )
        {
            String className =
                fileNameOfClass.substring( 0, fileNameOfClass.lastIndexOf( ".class" ) )
                    .replaceAll( "\\\\|/", "." );
            classNames.add( className );
        }
        return classNames;
    }

    /**
     * Scans a jar file for classes matching an ant-style path mapping.
     *
     * @param jarUrl  url of the jar file to be scanned
     * @param pattern ant-style pattern
     *
     * @return list of matching class names
     *
     * @throws IOException - If a serious problem appears while processing teh jar file
     */
    // TODO maybe extract thsi as a scanner?
    private static List<String> scanJar( final URL jarUrl,
                                         final String pattern )
        throws IOException
    {
        final List<String> classNames = new ArrayList<String>();
        final URLConnection connection = jarUrl.openConnection();
        if( !( connection instanceof JarURLConnection ) )
        {
            return classNames;
        }

        final JarURLConnection jarConnection = (JarURLConnection) connection;
        final JarFile jarFile = jarConnection.getJarFile();
        final String roothPath = jarConnection.getJarEntry().getName();

        for( Enumeration entries = jarFile.entries(); entries.hasMoreElements(); )
        {
            final JarEntry entry = (JarEntry) entries.nextElement();
            final String entryPath = entry.getName();

            if( entryPath.startsWith( roothPath ) &&
                entryPath.endsWith( ".class" ) &&
                SelectorUtils.matchPath( pattern, entryPath.substring( roothPath.length() + 1 ), true ) )
            {
                final String className = entryPath.substring( 0, entryPath.lastIndexOf( ".class" ) )
                    .replaceAll( "\\\\|/", "." );
                classNames.add( className );
            }
        }
        return classNames;
    }

    /**
     * Extract the starting substring from an ant-style pattern. This is teh part from start of pattern till last "/"
     * before the first "*" or "?".
     *
     * @param pattern ant-style pattern
     *
     * @return base pattern
     */
    private String extractBasePattern( final String pattern )
    {
        final int asterisk = pattern.indexOf( '*' );
        final int questionMarkIndex = pattern.indexOf( '?' );
        if( asterisk == -1 && questionMarkIndex == -1 )
        {
            return pattern;
        }
        final int wildcardStart = ( asterisk > questionMarkIndex ? asterisk : questionMarkIndex );
        int baseEndPos = pattern.lastIndexOf( '/', wildcardStart );
        return ( baseEndPos != -1 ? pattern.substring( 0, baseEndPos ) : "" );
    }

    /**
     * Returns a list of source directories to be scanned for components.
     * The list is composed from {@link #getDefaultSourceDirectories()}, {@link #getAdditionalSourceDirectories()} and
     * the dependent plugins directories.
     *
     * @return list of source directories (should not be null)
     */
    protected String[] getSourceDirectories()
    {
        final List<String> directories = new ArrayList<String>();
        final String[] defaultDirs = getDefaultSourceDirectories();
        if( defaultDirs != null && defaultDirs.length > 0 )
        {
            directories.addAll( Arrays.asList( defaultDirs ) );
        }
        final String[] additionalDirs = getAdditionalSourceDirectories();
        if( additionalDirs != null && additionalDirs.length > 0 )
        {
            directories.addAll( Arrays.asList( additionalDirs ) );
        }

        return directories.toArray( new String[directories.size()] );
    }

    /**
     * Returns a list of default directories to be scanned for components.
     *
     * @return list of source directories (should not be null)
     */
    protected String[] getDefaultSourceDirectories()
    {
        return sourceDirectories;
    }

    /**
     * Returns a list of additional directories to be scanned for components beside default ones.
     * By default the list is empty but can be overridden by tests in order to add additional directories.
     *
     * @return list of source directories (should not be null)
     */
    protected String[] getAdditionalSourceDirectories()
    {
        return new String[0];
    }

    /**
     * Returns a list of claspath entry paths to be scanned.
     *
     * @return list of classpath entry paths (should not be null)
     */
    protected String[] getClasspathEntries()
    {
        return new String[0];
    }

}
