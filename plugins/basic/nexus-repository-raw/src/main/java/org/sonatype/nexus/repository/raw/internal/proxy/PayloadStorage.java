package org.sonatype.nexus.repository.raw.internal.proxy;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Payload;

/**
 * @since 3.0
 */
public interface PayloadStorage
    extends Facet
{
  Payload get(Locator locator);

  void put(Locator locator, Payload payload);

  boolean delete(Locator locator);
}
