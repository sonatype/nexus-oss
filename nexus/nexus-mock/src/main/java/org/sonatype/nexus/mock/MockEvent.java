package org.sonatype.nexus.mock;

import org.restlet.data.Method;

public class MockEvent
{

    private Method method;

    private boolean blocked;

    protected final boolean isBlocked()
    {
        return blocked;
    }

    public Method getMethod()
    {
        return method;
    }

    public MockEvent( Method method )
    {
        super();
        this.method = method;
    }

    public void block()
    {
        this.blocked = true;
    }
}
