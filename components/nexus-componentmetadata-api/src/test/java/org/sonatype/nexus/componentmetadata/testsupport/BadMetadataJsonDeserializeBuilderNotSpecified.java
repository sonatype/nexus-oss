package org.sonatype.nexus.componentmetadata.testsupport;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
public class BadMetadataJsonDeserializeBuilderNotSpecified
    extends SecondaryMetadata
{
  private final String foo;

  private BadMetadataJsonDeserializeBuilderNotSpecified(String identityKey, @Nullable String foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public String getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadMetadataJsonDeserializeBuilderNotSpecified>
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
    public BadMetadataJsonDeserializeBuilderNotSpecified build() {
      return new BadMetadataJsonDeserializeBuilderNotSpecified(identityKey, foo);
    }
  }
}
