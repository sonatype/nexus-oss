package org.sonatype.nexus.proxy.events;

import java.util.List;

public interface Vetoable
{
    boolean isVetoed();

    void putVeto( Object veto );

    boolean removeVeto( Object veto );

    List<Object> getVetos();
}
