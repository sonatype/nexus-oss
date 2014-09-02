package org.sonatype.nexus.componentviews.config;

import java.io.IOException;

/**
 * A provider of (usually example/testing) view configuration, in the absence of a UI to define test configs.
 *
 * @since 3.0
 */
public interface ViewConfigContributor
{
  void contributeTo(ViewConfigStore store) throws IOException;
}
