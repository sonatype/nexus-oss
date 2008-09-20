/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security;

import java.util.Set;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.security.model.CPrivilege;
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
    
        manager = (DefaultPrivilegeInheritanceManager) this.lookup( PrivilegeInheritanceManager.ROLE );
    }
    
    public void testCreateInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( CPrivilege.METHOD_CREATE );
        
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( CPrivilege.METHOD_READ ) );
        assertTrue( methods.contains( CPrivilege.METHOD_CREATE ) );
    }
    
    public void testReadInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( CPrivilege.METHOD_READ );
        
        assertTrue( methods.size() == 1 );
        assertTrue( methods.contains( CPrivilege.METHOD_READ ) );
    }
    
 
    
    public void testUpdateInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( CPrivilege.METHOD_UPDATE );
            
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( CPrivilege.METHOD_READ ) );
        assertTrue( methods.contains( CPrivilege.METHOD_UPDATE ) );
    }
    
    public void testDeleteInherit()
        throws Exception
    {
        Set<String> methods = manager.getInheritedMethods( CPrivilege.METHOD_DELETE );
        
        assertTrue( methods.size() == 2 );
        assertTrue( methods.contains( CPrivilege.METHOD_READ ) );
        assertTrue( methods.contains( CPrivilege.METHOD_DELETE ) );
    }
}
