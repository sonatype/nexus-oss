package org.sonatype.nexus.repository.raw.internal.proxy;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.view.Context;

/**
 * TODO: Rename - this is more like a generic facet for adapting format-specific into standard stuff
 */
public interface LocatorFacet
    extends Facet
{
  Locator locator(Context context);
}
