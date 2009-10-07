package org.sonatype.nexus.proxy.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.plexus.appevents.AbstractEvent;

public class AbstractVetoableEvent<T>
    extends AbstractEvent<T>
    implements Vetoable
{
    private final ArrayList<Veto> vetos = new ArrayList<Veto>();

    public AbstractVetoableEvent( T component )
    {
        super( component );
    }

    public List<Veto> getVetos()
    {
        return Collections.unmodifiableList( vetos );
    }

    public boolean isVetoed()
    {
        return !vetos.isEmpty();
    }

    public void putVeto( Veto veto )
    {
        vetos.add( veto );
    }

    public void putVeto( Object vetoer, Throwable reason )
    {
        vetos.add( new Veto( vetoer, reason ) );
    }

    public boolean removeVeto( Veto veto )
    {
        return vetos.remove( veto );
    }

}
