package org.sonatype.nexus.proxy.registry;

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
}
