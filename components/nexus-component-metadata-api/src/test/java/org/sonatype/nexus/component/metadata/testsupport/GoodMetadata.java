package org.sonatype.nexus.component.metadata.testsupport;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.metadata.api.MetadataBuilder;
import org.sonatype.nexus.component.metadata.api.MetadataIndex;
import org.sonatype.nexus.component.metadata.api.MetadataType;
import org.sonatype.nexus.component.metadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = GoodMetadata.Builder.class)
public class GoodMetadata extends SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .unique()
      .build();

  public static final MetadataType<GoodMetadata> TYPE = new MetadataType.Builder<GoodMetadata>()
      .withName("good")
      .withMetadataClass(GoodMetadata.class)
      .withIndexes(INDEX_IDENTITYKEY)
      .build();

  private final String foo;

  private GoodMetadata(String identityKey, @Nullable String foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
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
