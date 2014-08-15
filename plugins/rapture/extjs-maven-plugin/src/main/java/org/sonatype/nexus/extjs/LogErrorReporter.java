package org.sonatype.nexus.extjs;

import org.apache.maven.plugin.logging.Log;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Mojo {@link Log} adapting {@link ErrorReporter}.
 *
 * @since 3.0
 */
public class LogErrorReporter
  implements ErrorReporter
{
  private final Log log;

  public LogErrorReporter(final Log log) {
    this.log = checkNotNull(log);
  }

  private String format(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
    return String.format("%s (%s#%d:%d): %s", message, sourceName, line, lineOffset, lineSource);
  }

  @Override
  public void warning(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
    log.warn(format(message, sourceName, line, lineSource, lineOffset));
  }

  @Override
  public void error(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset){
    log.error(format(message, sourceName, line, lineSource, lineOffset));
  }

  @Override
  public EvaluatorException runtimeError(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
    return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
  }
}
