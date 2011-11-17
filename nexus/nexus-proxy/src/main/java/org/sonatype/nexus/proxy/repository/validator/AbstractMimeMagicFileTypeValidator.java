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
package org.sonatype.nexus.proxy.repository.validator;

import java.io.IOException;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Helper base class for implementing {@link FileTypeValidator} components that want to verify the content's MIME magic
 * signature using {@link MimeSupport}. The main method {@link #isExpectedFileType(StorageFileItem)} has to be
 * implemented by implementor in a way it collects it's "expectations" from some source (internal state, some config,
 * whatever) and invokes the {@link #isExpectedFileTypeByDetectedMimeType(StorageFileItem, Set)} method with the file
 * item and the set of "expectations". If the set has intersection, file is claimed valid, otherwise invalid.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public abstract class AbstractMimeMagicFileTypeValidator
    extends AbstractFileTypeValidator
{
    @Requirement
    private MimeSupport mimeSupport;

    /**
     * This method accepts the file item which content needs MIME magic detection, and the set of "expectations" to
     * match against. If the detected set of MIME types and passed in set of MIME types has intersection, file is
     * claimed VALID, otherwise INVALID. If the passed in set of expectations is empty of {@code null}, NEUTRAL stance
     * is claimed and nothing is done.
     * 
     * @param file to have checked content.
     * @param expectedMimeTypes the "expectations" against detected MIME types.
     * @return {@link FileTypeValidity.VALID} if detected MIME types and passed in expectations has intersection,
     *         {@link FileTypeValidity.INVALID} otherwise. {@link FileTypeValidity.NEUTRAL} if passed in expectations
     *         are {@code null} or empty.
     * @throws IOException in case of some IO problem.
     */
    protected FileTypeValidity isExpectedFileTypeByDetectedMimeType( final StorageFileItem file,
                                                                     final Set<String> expectedMimeTypes )
        throws IOException
    {
        if ( expectedMimeTypes == null || expectedMimeTypes.isEmpty() )
        {
            // we have nothing to work against, cannot take side
            return FileTypeValidity.NEUTRAL;
        }

        final Set<String> magicMimeTypes = mimeSupport.detectMimeTypesFromContent( file.getContentLocator() );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Checking StorageFileItem {} is one of the expected mime types: {}, detected mime types are: {}",
                new Object[] { file.getRepositoryItemUid(), expectedMimeTypes, magicMimeTypes } );
        }

        for ( String magicMimeType : magicMimeTypes )
        {
            if ( expectedMimeTypes.contains( magicMimeType ) )
            {
                return FileTypeValidity.VALID;
            }
        }

        getLogger().info(
            "StorageFileItem {} MIME-magic validation failed: expected MIME types: {}, detected MIME types: {}",
            new Object[] { file.getRepositoryItemUid(), expectedMimeTypes, magicMimeTypes } );

        return FileTypeValidity.INVALID;
    }
}
