package org.sonatype.nexus.internal.orient;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link HexRecordIdObfuscator}.
 */
public class HexRecordIdObfuscatorTest
  extends TestSupport
{
  private HexRecordIdObfuscator underTest;

  @Before
  public void setUp() throws Exception {
    this.underTest = new HexRecordIdObfuscator();
  }

  @Test
  public void encodeAndDecode() {
    ORID rid = new ORecordId("#9:1");
    log("RID: {}", rid);
    String encoded = underTest.encode(rid);
    log("Encoded: {}", encoded);
    ORID decoded = underTest.decode(encoded);
    log("Decoded: {}", decoded);
    assertThat(decoded.toString(), is("#9:1"));
  }
}
