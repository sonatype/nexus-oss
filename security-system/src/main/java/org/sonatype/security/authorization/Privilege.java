package org.sonatype.security.authorization;

import java.util.Map;

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
    private Map<String, String> properties;
    
    private boolean readOnly;

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
    
    public void addProperty( String key, String value)
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
