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
package org.sonatype.nexus.configuration.security;

import java.util.Set;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.jsecurity.DefaultPrivilegeInheritanceManager;
import org.sonatype.nexus.jsecurity.PrivilegeInheritanceManager;

public class DefaultPrivilegeInheritanceManagerTest
    extends
    AbstractNexusTestCase
{
    private DefaultPrivilegeInheritanceManager manager;
    
    protected void setUp()
        throws Exception
    {
        super.setUp();
    
        manager = (DefaultPrivilegeInheritanceManager) this.lookup( PrivilegeInheritanceManager.class );
    }
    
    public void testCreateInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( "create" );
        
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "create" ) );
    }
    
    public void testReadInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( "read" );
        
        assertTrue( methods.size() == 1 );
        assertTrue( methods.contains( "read" ) );
    }
    
 
    
    public void testUpdateInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( "update" );
            
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "update" ) );
    }
    
    public void testDeleteInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( "delete" );
        
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( "read" ) );
        assertTrue( methods.contains( "delete" ) );
    }
}
