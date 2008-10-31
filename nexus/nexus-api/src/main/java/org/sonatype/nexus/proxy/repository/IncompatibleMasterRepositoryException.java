package org.sonatype.nexus.proxy.repository;

/**
 * Throws when an incompatible master is assigned to a shadow repository.
 * 
 * @author cstamas
 */
public class IncompatibleMasterRepositoryException
    extends Exception
{
    private static final long serialVersionUID = -5676236705854300582L;

    private final ShadowRepository shadow;

    private final Repository master;

    public IncompatibleMasterRepositoryException( ShadowRepository shadow, Repository master )
    {
        this( "Master repository ID='" + master.getId() + "' is incompatible with shadow repository ID='"
            + shadow.getId() + "' because of it's ContentClass", shadow, master );
    }

    public IncompatibleMasterRepositoryException( String message, ShadowRepository shadow, Repository master )
    {
        super( message );

        this.shadow = shadow;

        this.master = master;
    }

    public ShadowRepository getShadow()
    {
        return shadow;
    }

    public Repository getMaster()
    {
        return master;
    }
}
