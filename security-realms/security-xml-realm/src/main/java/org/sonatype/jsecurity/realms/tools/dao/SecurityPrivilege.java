/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.jsecurity.realms.tools.dao;

import java.util.List;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;

public class SecurityPrivilege
    extends CPrivilege
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityPrivilege()
    {
    }
    
    public SecurityPrivilege( CPrivilege privilege )
    {
        this( privilege, false );
    }
    
    public SecurityPrivilege( CPrivilege privilege, boolean readOnly )
    {
        setDescription( privilege.getDescription() );
        setId( privilege.getId() );
        setName( privilege.getName() );
        setType( privilege.getType() );
        setReadOnly( readOnly );
        
        if ( privilege.getProperties() != null )
        {
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                addProperty( new SecurityProperty( prop, true ) );
            }
        }
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }
}
