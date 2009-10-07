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
package org.sonatype.nexus.proxy.item;

import java.util.List;

/**
 * The Interface StorageCompositeItem. These items are "composed" from multiple other items, like merged
 * maven-metadata.xml.
 */
public interface StorageCompositeItem
    extends StorageItem
{
    /**
     * Lists this sources that makes up this composite item. The order is important, usually it is order how a group
     * processes it's members and items are fetched from them.
     * 
     * @return the sources for this composite item
     */
    List<StorageItem> getSources();
}
