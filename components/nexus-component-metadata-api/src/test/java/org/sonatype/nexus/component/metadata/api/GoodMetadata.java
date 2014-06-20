package org.sonatype.nexus.component.metadata.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = GoodMetadata.Builder.class)
class GoodMetadata extends SecondaryMetadata
{
  private final String foo;

  private GoodMetadata(String identityKey, String foo) {
    super(identityKey);
    this.foo = foo;
  }

  public String getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<GoodMetadata>
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
    public GoodMetadata build() {
      return new GoodMetadata(identityKey, foo);
    }
  }
}
