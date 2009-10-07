package org.sonatype.nexus.logging;

import javax.inject.Provider;

import org.slf4j.Logger;

public interface LoggerProvider
    extends Provider<Logger>
{
    Logger getLogger( String loggerKey );
}
