package org.sonatype.nexus.rest;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public class NoSuchRepositoryAccessException
    extends NoSuchRepositoryException
{
    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = 8302600889970064313L;

    public NoSuchRepositoryAccessException( String repoId )
    {
        super( repoId );
    }

}
