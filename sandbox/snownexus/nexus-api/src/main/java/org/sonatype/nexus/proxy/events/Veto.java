package org.sonatype.nexus.proxy.events;

public class Veto
{
    private final Object vetoer;

    private final Throwable reason;

    public Veto( Object vetoer, Throwable reason )
    {
        this.vetoer = vetoer;

        this.reason = reason;
    }

    public Object getVetoer()
    {
        return vetoer;
    }

    public Throwable getReason()
    {
        return reason;
    }
}
