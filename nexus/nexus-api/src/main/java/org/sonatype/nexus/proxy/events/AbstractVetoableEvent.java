package org.sonatype.nexus.proxy.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AbstractVetoableEvent
    extends AbstractEvent
    implements Vetoable
{
    private final ArrayList<Object> vetos = new ArrayList<Object>();

    public List<Object> getVetos()
    {
        return Collections.unmodifiableList( vetos );
    }

    public boolean isVetoed()
    {
        return !vetos.isEmpty();
    }

    public void putVeto( Object veto )
    {
        vetos.add( veto );
    }

    public boolean removeVeto( Object veto )
    {
        return vetos.remove( veto );
    }
}
