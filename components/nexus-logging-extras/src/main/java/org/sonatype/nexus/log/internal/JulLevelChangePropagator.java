package org.sonatype.nexus.log.internal;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.jul.JULHelper;

/**
 * Adapter to standard {@link ch.qos.logback.classic.jul.LevelChangePropagator}
 * to work around bug with null levels.
 *
 * @since 2.7
 */
public class JulLevelChangePropagator
  extends ch.qos.logback.classic.jul.LevelChangePropagator
{
  @Override
  public void onLevelChange(final Logger logger, final Level level) {
    if (level != null) {
      super.onLevelChange(logger, level);
    }
    else {
      java.util.logging.Logger julLogger = JULHelper.asJULLogger(logger);
      julLogger.setLevel(null);
    }
  }
}
