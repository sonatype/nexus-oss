package org.sonatype.nexus.proxy.access;

/**
 * Enum that represents the valid "actions" against Nexus path.
 * 
 * @author cstamas
 */
public enum Action
{
    read,

    create,

    update,

    delete;

    public boolean isReadAction()
    {
        return read.equals( this );
    }

    public boolean isWritingAction()
    {
        return ( create.equals( this ) || update.equals( this ) );
    }
}
