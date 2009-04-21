package org.sonatype.jsecurity.realms.tools;

public abstract class AbstractStaticSecurityResource
    implements StaticSecurityResource
{
    protected boolean dirty = false;
    
    public boolean isDirty()
    {
        return dirty;
    }
    
    protected void setDirty( boolean dirty )
    {
        this.dirty = dirty;
    }
}
