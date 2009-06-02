package org.sonatype.nexus.proxy.registry;

import org.codehaus.plexus.util.StringUtils;

/**
 * A simple descriptor for all roles implementing a Nexus Repository.
 * 
 * @author cstamas
 */
public class RepositoryTypeDescriptor
{
    private String role;

    private String prefix;

    public RepositoryTypeDescriptor()
    {
        // empty
    }

    public RepositoryTypeDescriptor( String role, String prefix )
    {
        this();

        this.role = role;

        this.prefix = prefix;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole( String role )
    {
        this.role = role;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix( String prefix )
    {
        this.prefix = prefix;
    }

    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }

        if ( o == null || ( o.getClass() != this.getClass() ) )
        {
            return false;
        }

        RepositoryTypeDescriptor other = (RepositoryTypeDescriptor) o;

        return StringUtils.equals( getRole(), other.getRole() ) && StringUtils.equals( getPrefix(), other.getPrefix() );
    }

    public int hashCode()
    {
        int result = 7;

        result = 31 * result + ( role == null ? 0 : role.hashCode() );

        result = 31 * result + ( prefix == null ? 0 : prefix.hashCode() );

        return result;
    }
}
