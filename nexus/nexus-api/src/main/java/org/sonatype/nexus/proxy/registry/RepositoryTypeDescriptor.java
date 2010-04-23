package org.sonatype.nexus.proxy.registry;

import org.codehaus.plexus.util.StringUtils;

/**
 * A simple descriptor for all roles implementing a Nexus Repository.
 * 
 * @author cstamas
 */
public class RepositoryTypeDescriptor
{
    private final String role;
    
    private final String hint;

    private final String prefix;

    public RepositoryTypeDescriptor( String role, String hint, String prefix )
    {
        this.role = role;
        
        this.hint = hint;

        this.prefix = prefix;
    }

    public String getRole()
    {
        return role;
    }

    public String getHint()
    {
        return hint;
    }

    public String getPrefix()
    {
        return prefix;
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

        return StringUtils.equals( getRole(), other.getRole() ) && StringUtils.equals( getHint(), other.getHint() ) && StringUtils.equals( getPrefix(), other.getPrefix() );
    }

    public int hashCode()
    {
        int result = 7;

        result = 31 * result + ( role == null ? 0 : role.hashCode() );

        result = 31 * result + ( hint == null ? 0 : hint.hashCode() );

        result = 31 * result + ( prefix == null ? 0 : prefix.hashCode() );

        return result;
    }
}
