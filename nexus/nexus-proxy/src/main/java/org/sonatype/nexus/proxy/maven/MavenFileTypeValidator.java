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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.validator.AbstractMimeMagicFileTypeValidator;
import org.sonatype.nexus.proxy.repository.validator.FileTypeValidator;
import org.sonatype.nexus.proxy.repository.validator.XMLUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * Maven specific FileTypeValidator that checks for "most common" Maven artifacts and metadatas, namely: JARs, ZIPs,
 * WARs, EARs, POMs and XMLs.
 * 
 * @author cstamas
 */
@Component( role = FileTypeValidator.class, hint = "maven" )
public class MavenFileTypeValidator
    extends AbstractMimeMagicFileTypeValidator
{
    public static final String XML_DETECTION_LAX_KEY = MavenFileTypeValidator.class.getName() + ".relaxedXmlValidation";

    private static final boolean XML_DETECTION_LAX_DEFAULT = true;

    private static final boolean XML_DETECTION_LAX = SystemPropertiesHelper.getBoolean( XML_DETECTION_LAX_KEY,
        XML_DETECTION_LAX_DEFAULT );

    private Map<String, List<String>> supportedTypeMap = new HashMap<String, List<String>>();

    public MavenFileTypeValidator()
    {
        supportedTypeMap.put( "jar", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "zip", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "war", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "ear", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "pom", Arrays.asList( "application/x-maven-pom", "application/xml", "text/xml" ) );
        supportedTypeMap.put( "xml", Arrays.asList( "application/xml", "text/xml" ) );
        supportedTypeMap.put( "tar", Arrays.asList( "application/x-tar" ) );
        // flex
        supportedTypeMap.put( "swc", Arrays.asList( "application/zip" ) );
        supportedTypeMap.put( "swf", Arrays.asList( "application/x-shockwave-flash" ) );
        // tar.gz
        supportedTypeMap.put( "gz", Arrays.asList( "application/x-gzip", "application/x-tgz" ) );
        supportedTypeMap.put( "tgz", Arrays.asList( "application/x-tgz" ) );

        // tar.bz2
        supportedTypeMap.put( "bz2", Arrays.asList( "application/x-bzip2" ) );
        supportedTypeMap.put( "tbz", Arrays.asList( "application/x-bzip2" ) );
    }

    @Override
    public FileTypeValidity isExpectedFileType( final StorageFileItem file )
    {
        // Note: this here is an ugly hack: enables per-request control of
        // LAX XML validation: if key not present, "system wide" settings used.
        // If key present, it's interpreted as Boolean and it's value is used to
        // drive LAX XML validation enable/disable.
        boolean xmlLaxValidation = XML_DETECTION_LAX;
        if ( file.getItemContext().containsKey( XML_DETECTION_LAX_KEY ) )
        {
            xmlLaxValidation =
                Boolean.parseBoolean( String.valueOf( file.getItemContext().get( XML_DETECTION_LAX_KEY ) ) );
        }

        final String filePath = file.getPath().toLowerCase();
        if ( filePath.endsWith( ".pom" ) )
        {
            getLogger().debug( "Checking if Maven POM {} is of the correct MIME type.", file.getRepositoryItemUid() );

            try
            {
                return XMLUtils.validateXmlLikeFile( file, "<project" );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e );

                return FileTypeValidity.NEUTRAL;
            }
        }
        else if ( filePath.endsWith( "/maven-metadata.xml" ) )
        {
            getLogger().debug( "Checking if Maven Repository Metadata {} is of the correct MIME type.",
                file.getRepositoryItemUid() );

            try
            {
                return XMLUtils.validateXmlLikeFile( file, "<metadata" );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e );

                return FileTypeValidity.NEUTRAL;
            }
        }
        else if ( xmlLaxValidation && filePath.endsWith( ".xml" ) )
        {
            getLogger().debug( "Checking if XML {} is of the correct MIME type (lax=enabled).",
                file.getRepositoryItemUid() );

            Set<String> expectedMimeTypes = new HashSet<String>();

            for ( Entry<String, List<String>> entry : supportedTypeMap.entrySet() )
            {
                if ( filePath.endsWith( entry.getKey() ) )
                {
                    expectedMimeTypes.addAll( entry.getValue() );
                }
            }

            try
            {
                final FileTypeValidity mimeDetectionResult =
                    isExpectedFileTypeByDetectedMimeType( file, expectedMimeTypes );

                if ( FileTypeValidity.INVALID.equals( mimeDetectionResult ) )
                {
                    // we go LAX way, if MIME detection says INVALID (does for XMLs missing preamble too)
                    // we just stay put saying we are "neutral" on this question
                    // If LAX disabled, the strict check will happen at the end of this if-else anyway
                    // doing proper checks and proper INVALID
                    return FileTypeValidity.NEUTRAL;
                }
                else
                {
                    return mimeDetectionResult;
                }
            }
            catch ( IOException e )
            {
                getLogger().warn(
                    "Cannot detect MIME type and validate content of StorageFileItem: " + file.getRepositoryItemUid(),
                    e );

                return FileTypeValidity.NEUTRAL;
            }
        }
        else if ( filePath.endsWith( ".sha1" ) || filePath.endsWith( ".md5" ) )
        {
            getLogger().debug( "Checking if Maven checksum {} is valid.", file.getRepositoryItemUid() );

            try
            {
                final String digest = MUtils.readDigestFromFileItem( file );
                if ( MUtils.isDigest( digest ) )
                {
                    if ( filePath.endsWith( ".sha1" ) && digest.length() == 40 )
                    {
                        return FileTypeValidity.VALID;
                    }
                    if ( filePath.endsWith( ".md5" ) && digest.length() == 32 )
                    {
                        return FileTypeValidity.VALID;
                    }
                }
                return FileTypeValidity.INVALID;
            }
            catch ( IOException e )
            {
                getLogger().warn( "Cannot access content of StorageFileItem: " + file.getRepositoryItemUid(), e );

                return FileTypeValidity.NEUTRAL;
            }

        }
        else
        {
            Set<String> expectedMimeTypes = new HashSet<String>();

            for ( Entry<String, List<String>> entry : supportedTypeMap.entrySet() )
            {
                if ( filePath.endsWith( entry.getKey() ) )
                {
                    expectedMimeTypes.addAll( entry.getValue() );
                }
            }
            try
            {
                // the expectedMimeTypes will be empty, see map in constructor which extensions we check at all.
                // The isExpectedFileTypeByDetectedMimeType() method will claim NEUTRAL when expectancies are empty/null
                return isExpectedFileTypeByDetectedMimeType( file, expectedMimeTypes );
            }
            catch ( IOException e )
            {
                getLogger().warn(
                    "Cannot detect MIME type and validate content of StorageFileItem: " + file.getRepositoryItemUid(),
                    e );

                return FileTypeValidity.NEUTRAL;
            }
        }
    }
}
