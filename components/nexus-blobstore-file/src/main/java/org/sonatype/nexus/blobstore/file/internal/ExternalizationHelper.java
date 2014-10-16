package org.sonatype.nexus.blobstore.file.internal;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nullable;

/**
 * Helper methods for externalizing primitives to {@link ObjectOutput} instances.
 *
 * @since 3.0
 */
public class ExternalizationHelper
{
  /**
   * Writes a possibly null {@link Long} to an {@link ObjectOutput}. Read the value using {@link
   * #readNullableLong(ObjectInput)}.
   */
  public static void writeNullableLong(ObjectOutput out, Long value) throws IOException {
    out.writeBoolean(value != null);
    if (value != null) {
      out.writeLong(value);
    }
  }

  @Nullable
  public static Long readNullableLong(ObjectInput in) throws IOException {
    if (in.readBoolean()) {
      return in.readLong();
    }
    return null;
  }

  /**
   * Writes a possibly null {@link String} to an {@link ObjectOutput}. Read the value using {@link
   * #readNullableString(ObjectInput)}.
   */
  public static void writeNullableString(ObjectOutput out, String value) throws IOException {
    out.writeBoolean(value != null);
    if (value != null) {
      out.writeUTF(value);
    }
  }

  @Nullable
  public static String readNullableString(ObjectInput in) throws IOException {
    if (in.readBoolean()) {
      return in.readUTF();
    }
    return null;
  }
}
