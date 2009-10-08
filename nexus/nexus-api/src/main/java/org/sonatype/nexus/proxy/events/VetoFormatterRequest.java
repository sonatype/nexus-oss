package org.sonatype.nexus.proxy.events;

public class VetoFormatterRequest
{
    private Vetoable event;
    
    private boolean detailed;
    
    public VetoFormatterRequest( Vetoable event, boolean detailed )
    {
        this.event = event;
        this.detailed = detailed;
    }
    
    public Vetoable getEvent()
    {
        return event;
    }
    
    public boolean isDetailed()
    {
        return detailed;
    }
}
