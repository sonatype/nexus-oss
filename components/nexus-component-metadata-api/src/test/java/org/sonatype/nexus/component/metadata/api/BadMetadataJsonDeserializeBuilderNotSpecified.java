package org.sonatype.nexus.component.metadata.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
class BadMetadataJsonDeserializeBuilderNotSpecified
    extends SecondaryMetadata
{
  private final String foo;

  private BadMetadataJsonDeserializeBuilderNotSpecified(String identityKey, String foo) {
    super(identityKey);
    this.foo = foo;
  }

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
