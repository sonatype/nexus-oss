package org.sonatype.nexus.component.metadata.testsupport;

import org.sonatype.nexus.component.metadata.api.AbstractMetadataTypeRegistry;
import org.sonatype.nexus.component.metadata.api.MetadataType;

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
