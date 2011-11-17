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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Scanner;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.validator.FileTypeValidator.FileTypeValidity;

/**
 * Static helper methods to make "XML like" files probation against some patterns or expected content easier and
 * reusable.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class XMLUtils
{
    /**
     * Validate an "XML like file" using at most 200 lines from it's beginning. See
     * {@link #validateXmlLikeFile(StorageFileItem, String, int)} for details.
     * 
     * @param file file who's content needs to be checked for.
     * @param expectedPattern the expected String pattern to search for.
     * @return {@link FileTypeValidity.VALID} if pattern found, {@link FileTypeValidity.INVALID} otherwise.
     * @throws IOException in case of IO problem while reading file content.
     */
    public static FileTypeValidity validateXmlLikeFile( final StorageFileItem file, final String expectedPattern )
        throws IOException
    {
        return validateXmlLikeFile( file, expectedPattern, 200 );
    }

    /**
     * Validate an "XML like file" by searching for passed in patterns (using plain string matching), consuming at most
     * lines as passed in as parameter.
     * 
     * @param file file who's content needs to be checked for.
     * @param expectedPattern the expected String pattern to search for.
     * @param linesToCheck amount of lines (as detected by Scanner) to consume during check.
     * @return {@link FileTypeValidity.VALID} if pattern found, {@link FileTypeValidity.INVALID} otherwise.
     * @throws IOException in case of IO problem while reading file content.
     */
    public static FileTypeValidity validateXmlLikeFile( final StorageFileItem file, final String expectedPattern,
                                                        final int linesToCheck )
        throws IOException
    {
        int lineCount = 0;
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream( file.getInputStream() );
            Scanner scanner = new Scanner( bis );
            while ( scanner.hasNextLine() && lineCount < linesToCheck )
            {
                lineCount++;
                String line = scanner.nextLine();
                if ( line.contains( expectedPattern ) )
                {
                    return FileTypeValidity.VALID;
                }
            }
        }
        finally
        {
            IOUtil.close( bis );
        }

        return FileTypeValidity.INVALID;
    }

}
