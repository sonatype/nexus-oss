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
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.OutputStream;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * Component handling links, and their persistence to storage. This component does not handle metadata, only the link
 * content solely. How to represent links on storage may vary, and has nothing to do where is storage located or what
 * technology is used for it. Hence, this component helps to handle links in storage independent way, and separates that
 * logic from LocalRepositoryStorage implementations. Some implementation may still handle the problem in alternate ways
 * (not using this component), but the default is given here.
 * 
 * @author cstamas
 */
public interface LinkPersister
{
    /**
     * Uses ContentLocator to inspect the supplied content and decide whether the content holds a "serialized" link form
     * or not.
     * 
     * @param locator
     * @return
     * @throws IOException
     */
    boolean isLinkContent( final ContentLocator locator )
        throws IOException;

    /**
     * Reads the link content "serialized" form, and creates the UID of the target, if possible.
     * 
     * @param locator
     * @return
     * @throws NoSuchRepositoryException
     * @throws IOException
     */
    RepositoryItemUid readLinkContent( final ContentLocator locator )
        throws NoSuchRepositoryException, IOException;

    /**
     * Writes "serialized" form of the link into the supplied output stream. It does flush if write is succesful. It
     * will always try to close the output stream, even in case of an IOException.
     * 
     * @param link
     * @param os
     * @throws IOException
     */
    void writeLinkContent( StorageLinkItem link, OutputStream os )
        throws IOException;
}
