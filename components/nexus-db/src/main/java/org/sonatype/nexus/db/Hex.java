package org.sonatype.nexus.db;

import com.google.common.base.Throwables;
import org.apache.commons.codec.DecoderException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHex;

/**
 * HEX encoding helpers.
 *
 * @since 3.0
 */
public class Hex
{
  /**
   * HEX encode bytes to string.
   */
  public static String encode(final byte[] bytes) {
    checkNotNull(bytes);
    char[] encoded = encodeHex(bytes);
    return new String(encoded);
  }

  /**
   * HEX decode string to bytes.
   */
  public static byte[] decode(final String encoded) {
    checkNotNull(encoded);
    try {
      return decodeHex(encoded.toCharArray());
    }
    catch (DecoderException e) {
      throw Throwables.propagate(e);
    }
  }
}
