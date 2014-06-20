package org.sonatype.nexus.component.metadata.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = BadMetadataNotSecondarySubclass.Builder.class)
class BadMetadataNotSecondarySubclass
{
  private final String identityKey;

  private final String foo;

  private BadMetadataNotSecondarySubclass(String identityKey, String foo) {
    this.identityKey = identityKey;
    this.foo = foo;
  }

  public String getIdentityKey() {
    return identityKey;
  }

  public String getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadMetadataNotSecondarySubclass>
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
    public BadMetadataNotSecondarySubclass build() {
      return new BadMetadataNotSecondarySubclass(identityKey, foo);
    }
  }
}
