package org.sonatype.nexus.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;

public final class NoopEventTarget
    implements EventTarget
{
    final static NoopEventTarget INSTANCE = new NoopEventTarget();

    @Override
    public void onEvent( final ILoggingEvent event )
    {
        // noop
    }
}
