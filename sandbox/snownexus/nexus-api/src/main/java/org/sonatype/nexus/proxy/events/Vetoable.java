package org.sonatype.nexus.proxy.events;

import java.util.List;

public interface Vetoable
{
    boolean isVetoed();

    void putVeto( Veto veto );

    void putVeto( Object vetoer, Throwable reason );

    boolean removeVeto( Veto veto );

    List<Veto> getVetos();
}
