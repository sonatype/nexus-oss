package org.sonatype.nexus.componentmetadata.testsupport;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.MetadataBuilder;
import org.sonatype.nexus.componentmetadata.api.MetadataIndex;
import org.sonatype.nexus.componentmetadata.api.MetadataType;
import org.sonatype.nexus.componentmetadata.api.SecondaryMetadata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = GoodMetadataSimple.Builder.class)
public class GoodMetadataSimple
    extends SecondaryMetadata
{
  public static final MetadataIndex INDEX_IDENTITYKEY = new MetadataIndex.Builder()
      .withColumns("identityKey")
      .unique()
      .build();

  public static final MetadataType<GoodMetadataSimple> TYPE = new MetadataType.Builder<GoodMetadataSimple>()
      .withName("good")
      .withMetadataClass(GoodMetadataSimple.class)
      .withIndexes(INDEX_IDENTITYKEY)
      .build();

  private final String foo;

  private GoodMetadataSimple(String identityKey, @Nullable String foo) {
    super(identityKey);
    this.foo = foo;
  }

  @Nullable
  public String getFoo() {
    return foo;
  }

  public static class Builder
      implements MetadataBuilder<GoodMetadataSimple>
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
    public GoodMetadataSimple build() {
      return new GoodMetadataSimple(identityKey, foo);
    }
  }
}
