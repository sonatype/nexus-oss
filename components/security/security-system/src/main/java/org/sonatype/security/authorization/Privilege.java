/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.authorization;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple bean that represents a privilge.
 * 
 * @author Brian Demers
 */
public class Privilege
{

    /**
     * Field id
     */
    private String id;

    /**
     * Field name
     */
    private String name;

    /**
     * Field description
     */
    private String description;

    /**
     * Field type
     */
    private String type;

    /**
     * Field properties
     */
    private Map<String, String> properties = new HashMap<String, String>();

    private boolean readOnly;

    public Privilege()
    {

    }

    public Privilege( String id, String name, String description, String type, Map<String, String> properties,
                      boolean readOnly )
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.properties = properties;
        this.readOnly = readOnly;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void addProperty( String key, String value )
    {
        this.properties.put( key, value );
    }

    public void setProperties( Map<String, String> properties )
    {
        this.properties = properties;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }

    public String getPrivilegeProperty( String key )
    {
        return this.properties.get( key );
    }

}
