package org.sonatype.nexus.repository.raw;

import org.sonatype.nexus.repository.Facet;

/**
 * TODO: This is ridiculous, and this should be migrated into a more fully featured facet that can retrieve remote
 * content.
 *
 * @since 3.0
 */
public interface RawRemoteSourceFacet
    extends Facet
{
  String getRemoteUrlBase();
}
