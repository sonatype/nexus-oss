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

import org.sonatype.security.model.CProperty;

public class SecurityProperty
    extends CProperty
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityProperty()
    {
    }
    
    public SecurityProperty( CProperty property )
    {
        this( property, false );
    }
    
    public SecurityProperty( CProperty property, boolean readOnly )
    {
        setKey( property.getKey() );
        setValue( property.getValue() );
        setReadOnly( readOnly );
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
