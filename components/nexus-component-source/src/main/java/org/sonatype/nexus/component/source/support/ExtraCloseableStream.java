package org.sonatype.nexus.component.source.support;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * A decorator that gives an InputStream the ability to close an additional {@link Closeable} resource when the
 * InputStream is closed.
 */
public class ExtraCloseableStream
    extends FilterInputStream
{
  final Closeable needsClosing;

  public ExtraCloseableStream(final InputStream in, final Closeable needsClosing) {
    super(in);
    this.needsClosing = needsClosing;
  }

  @Override
  public void close() throws IOException {
    super.close();
    IOUtils.closeQuietly(needsClosing);
  }
}
