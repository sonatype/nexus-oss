package com.sonatype.nexus.repository.nuget.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * Renders an InputStream re-readable by caching its content to a temporary file.
 *
 * @since 3.0
 */
public class TempStreamSupplier
    extends ComponentSupport
    implements AutoCloseable, Supplier<InputStream>
{
  private final Path tempFile;

  public TempStreamSupplier(final InputStream inputStream) throws IOException {
    tempFile = Files.createTempFile("", "");
  }

  @Override
  public void close() {
    try {
      Files.delete(tempFile);
    }
    catch (IOException e) {
      log.warn("Unable to delete temp file {}", tempFile, e);
    }
  }

  @Override
  public InputStream get() {
    try {
      return Files.newInputStream(tempFile);
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
