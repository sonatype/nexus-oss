package org.sonatype.nexus.componentmetadata.testsupport;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = BadAttributesUnsupportedType.Builder.class)
public class BadAttributesUnsupportedType
    extends SecondaryMetadata
{
  private final Object foo;

  private BadAttributesUnsupportedType(String identityKey, @Nullable Object foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public Object getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<BadAttributesUnsupportedType>
  {
    private String identityKey;
    private Object foo;

    public Builder withIdentityKey(String identityKey) {
      this.identityKey = identityKey;
      return this;
    }

    public Builder withFoo(Object foo) {
      this.foo = foo;
      return this;
    }

    @Override
    public BadAttributesUnsupportedType build() {
      return new BadAttributesUnsupportedType(identityKey, foo);
    }
  }
}
