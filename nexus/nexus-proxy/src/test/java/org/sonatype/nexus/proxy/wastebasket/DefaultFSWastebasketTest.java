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
package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;

/**
 * @author Juven Xu
 */
public class DefaultFSWastebasketTest
    extends AbstractNexusTestCase
{
    DefaultFSWastebasketForTest wastebasket;
    
    private File trashDir = null;
    private File wastebasketDir = null;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        trashDir = new File( getPlexusHomeDir(), "trash" );
        trashDir.mkdir();
        
        wastebasketDir = new File( getPlexusHomeDir(), "wastebasket" );
        wastebasketDir.mkdir();

        wastebasket = new DefaultFSWastebasketForTest();
    }
    
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        
        cleanDir( trashDir );
        cleanDir( wastebasketDir );
    }

    public void testDeleteFile()
        throws Exception
    {
        new File( wastebasketDir, "folderB" ).mkdirs();
        new File( wastebasketDir, "folderA/folderAA" ).mkdirs();
        new File( wastebasketDir, "folderA/folderAA/fileA.txt" ).createNewFile();
        new File( wastebasketDir, "folderB/fileB.txt" ).createNewFile();

        wastebasket.delete( wastebasketDir );

        File fileA = new File( trashDir, "wastebasket/folderA/folderAA/fileA.txt" );
        File fileB = new File( trashDir, "wastebasket/folderB/fileB.txt" );
        File folderAA = new File( trashDir, "wastebasket/folderA/folderAA" );

        assertTrue( fileA.exists() && fileA.isFile() );
        assertTrue( fileB.exists() && fileB.isFile() );
        assertTrue( folderAA.exists() && folderAA.isDirectory() );
    }

    class DefaultFSWastebasketForTest
        extends DefaultFSWastebasket
    {
        /**
         * Override for supplying a test trash directory
         */
        @Override
        public File getWastebasketDirectory()
        {
            return trashDir;
        }

        public void delete( File file )
            throws IOException
        {
            super.delete( file, false );
        }
    }
}
