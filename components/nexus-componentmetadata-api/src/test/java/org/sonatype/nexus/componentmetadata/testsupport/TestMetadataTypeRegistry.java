package org.sonatype.nexus.componentmetadata.testsupport;

import org.sonatype.nexus.componentmetadata.api.AbstractMetadataTypeRegistry;
import org.sonatype.nexus.componentmetadata.api.MetadataType;

public class TestMetadataTypeRegistry
    extends AbstractMetadataTypeRegistry
{
  private MetadataType lastType;

  public MetadataType getLastType() {
    return lastType;
  }

  @Override
  protected void configureType(final MetadataType type) {
    lastType = type;
  }
}
