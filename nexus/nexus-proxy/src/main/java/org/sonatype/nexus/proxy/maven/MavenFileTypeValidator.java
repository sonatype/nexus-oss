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
package org.sonatype.nexus.proxy.maven;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.validator.FileTypeValidator;

/**
 * Maven specific FileTypeValidator that checks for "most common" Maven artifacts and metadatas, namely: JARs, ZIPs,
 * WARs, EARs, POMs and XMLs.
 * 
 * @author cstamas
 */
@Component( role = FileTypeValidator.class, hint = "maven" )
public class MavenFileTypeValidator
    implements FileTypeValidator
{
    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    @Requirement
    private MimeUtil mimeUtil;

    private Map<String, List<String>> supportedTypeMap = new HashMap<String, List<String>>();

    public MavenFileTypeValidator()
    {
        supportedTypeMap.put( "jar", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "zip", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "war", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "ear", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "pom", Arrays.asList( "application/x-maven-pom", "application/xml", "text/xml" ) );
        supportedTypeMap.put( "xml", Arrays.asList( "application/xml", "text/xml" ) );
    }

    @Override
    public FileTypeValidity isExpectedFileType( final StorageFileItem file )
    {
        if ( file.getPath().toLowerCase().endsWith( "pom" ) )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Checking if Maven POM: " + file.getRepositoryItemUid().toString()
                    + " is of the correct MIME type." );
            }

            int lineCount = 0; // only process a few lines
            BufferedInputStream bis = null;
            try
            {
                bis = new BufferedInputStream( file.getInputStream() );
                Scanner scanner = new Scanner( bis );

                while ( scanner.hasNextLine() && lineCount < 200 )
                {
                    lineCount++;

                    String line = scanner.nextLine();

                    if ( line.contains( "<project" ) )
                    {
                        return FileTypeValidity.VALID;
                    }
                }
            }
            catch ( IOException e )
            {
                logger.warn( "Cannot access content of StorageFileItem: " + file.getRepositoryItemUid().toString(), e );

                return FileTypeValidity.NEUTRAL;
            }
            finally
            {
                IOUtil.close( bis );
            }

            return FileTypeValidity.INVALID;
        }
        else
        {
            Set<String> expectedMimeTypes = new HashSet<String>();

            for ( Entry<String, List<String>> entry : supportedTypeMap.entrySet() )
            {
                if ( file.getPath().toLowerCase().endsWith( entry.getKey() ) )
                {
                    expectedMimeTypes.addAll( entry.getValue() );
                }
            }

            return isExpectedFileType( file, expectedMimeTypes );
        }
    }

    // ==

    protected FileTypeValidity isExpectedFileType( final StorageFileItem file, final Set<String> expectedMimeTypes )
    {
        if ( expectedMimeTypes == null || expectedMimeTypes.isEmpty() )
        {
            // we have nothing to work against, cannot take side
            return FileTypeValidity.NEUTRAL;
        }

        Set<String> magicMimeTypes = new HashSet<String>();
        BufferedInputStream bis = null;

        try
        {
            magicMimeTypes.addAll( mimeUtil.getMimeTypes( bis = new BufferedInputStream( file.getInputStream() ) ) );
        }
        catch ( IOException e )
        {
            logger.warn( "Cannot access content of StorageFileItem: " + file.getRepositoryItemUid().toString(), e );

            return FileTypeValidity.NEUTRAL;
        }
        finally
        {
            IOUtil.close( bis );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Checking StorageFileItem " + file.getRepositoryItemUid().toString()
                + " is one of the expected mime types: " + expectedMimeTypes + ", detected mime types are: "
                + magicMimeTypes );
        }

        for ( String magicMimeType : magicMimeTypes )
        {
            if ( expectedMimeTypes.contains( magicMimeType ) )
            {
                return FileTypeValidity.VALID;
            }
        }

        return FileTypeValidity.INVALID;
    }
}
