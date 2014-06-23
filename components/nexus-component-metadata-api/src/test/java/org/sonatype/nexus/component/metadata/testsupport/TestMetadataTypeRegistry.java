package org.sonatype.nexus.component.metadata.testsupport;

import org.sonatype.nexus.component.metadata.api.MetadataType;
import org.sonatype.nexus.component.metadata.api.MetadataTypeRegistry;

public class TestMetadataTypeRegistry
    extends MetadataTypeRegistry
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
