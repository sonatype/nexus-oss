package org.sonatype.nexus.component.metadata.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = BadMetadataJsonDeserializeBuilderNotBuilder.Builder.class)
class BadMetadataJsonDeserializeBuilderNotBuilder
    extends SecondaryMetadata
{
  private final String foo;

  private BadMetadataJsonDeserializeBuilderNotBuilder(String identityKey, String foo) {
    super(identityKey);
    this.foo = foo;
  }

  public String getFoo() {
    return foo;
  }

  public static class Builder
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

    public BadMetadataJsonDeserializeBuilderNotBuilder build() {
      return new BadMetadataJsonDeserializeBuilderNotBuilder(identityKey, foo);
    }
  }
}
