package org.sonatype.nexus.componentmetadata.testsupport;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

public class BadMetadataJsonDeserializeNotPresent
    extends SecondaryMetadata
{
  private final String foo;

  private BadMetadataJsonDeserializeNotPresent(String identityKey, @Nullable String foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public String getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadMetadataJsonDeserializeNotPresent>
  {
    private String identityKey;
    private String foo;

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withFoo(String foo) {
      this.foo = foo;
      return this;
    }

    @Override
    public BadMetadataJsonDeserializeNotPresent build() {
      return new BadMetadataJsonDeserializeNotPresent(identityKey, foo);
    }
  }
}
