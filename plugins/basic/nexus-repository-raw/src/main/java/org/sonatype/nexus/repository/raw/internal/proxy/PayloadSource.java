package org.sonatype.nexus.repository.raw.internal.proxy;

import org.sonatype.nexus.repository.view.Payload;

/**
 * @since 3.0
 */ // TODO: Figure out a better arrangement for these interfaces
public interface PayloadSource
{
  Payload get(Locator locator);
}
