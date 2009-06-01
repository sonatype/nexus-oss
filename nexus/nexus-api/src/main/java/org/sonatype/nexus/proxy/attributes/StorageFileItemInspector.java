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
package org.sonatype.nexus.proxy.attributes;

import java.io.File;
import java.util.Set;

import javax.inject.Singleton;

import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.plexus.plugin.ExtensionPoint;

/**
 * The Interface StorageFileItemInspector.
 */
@ExtensionPoint
@Singleton
public interface StorageFileItemInspector
{
    /**
     * Gets the indexable keywords.
     * 
     * @return the indexable keywords
     */
    Set<String> getIndexableKeywords();

    /**
     * Checks if item is handled.
     * 
     * @param item the item
     * @return true, if is handled
     */
    boolean isHandled( StorageItem item );

    /**
     * Process storage file item.
     * 
     * @param item the item
     * @param file the file
     * @throws Exception the exception
     */
    void processStorageFileItem( StorageFileItem item, File file )
        throws Exception;

}
