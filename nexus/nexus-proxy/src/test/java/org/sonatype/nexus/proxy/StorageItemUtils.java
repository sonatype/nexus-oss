/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.item.StorageItem;

public class StorageItemUtils
{

    public static void printStorageItemList( List<StorageItem> items )
    {
        PrintWriter pw = new PrintWriter( System.out );
        pw.println( " *** List of StorageItems:" );
        for ( StorageItem item : items )
        {
            printStorageItem( pw, item );
        }
        pw.println( " *** List of StorageItems end" );
        pw.flush();
    }

    public static void printStorageItem( StorageItem item )
    {
        printStorageItem( new PrintWriter( System.out ), item );
    }

    public static void printStorageItem( PrintWriter pw, StorageItem item )
    {
        pw.println( item.getClass().getName() );
        Map<String, String> dataMap = item.getAttributes();
        for ( String key : dataMap.keySet() )
        {
            pw.print( key );
            pw.print( " = " );
            pw.print( dataMap.get( key ) );
            pw.println();
        }
        pw.println();
        pw.flush();
    }

}
