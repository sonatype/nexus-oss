package org.sonatype.security.ldap.upgrade.cipher;

/*
 * Copyright (C) 2008 Sonatype Inc.
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 * @author Oleg Gusakov
 * 
 */
public interface PlexusCipher
{
  public static String     ROLE                              = PlexusCipher.class
                                                                 .getName();

  public static final char ENCRYPTED_STRING_DECORATION_START = '{';
  public static final char ENCRYPTED_STRING_DECORATION_STOP  = '}';

  /**
   * encrypt given string with the given passPhrase and encode it into base64
   * 
   * @param str
   * @param passPhrase
   * @return
   * @throws PlexusCipherException
   */
  String encrypt(
      String str,
      String passPhrase )
      throws PlexusCipherException;

  /**
   * encrypt given string with the given passPhrase, encode it into base64 and
   * return result, wrapped into { } decorations
   * 
   * @param str
   * @param passPhrase
   * @return
   * @throws PlexusCipherException
   */
  String encryptAndDecorate(
      String str,
      String passPhrase )
      throws PlexusCipherException;

  /**
   * decrypt given base64 encrypted string
   * 
   * @param str
   * @param passPhrase
   * @return
   * @throws PlexusCipherException
   */
  String decrypt(
      String str,
      String passPhrase )
      throws PlexusCipherException;

  /**
   * decrypt given base64 encoded encrypted string. If string is decorated,
   * decrypt base64 encoded string inside decorations
   * 
   * @param str
   * @param passPhrase
   * @return
   * @throws PlexusCipherException
   */
  String decryptDecorated(
      String str,
      String passPhrase )
      throws PlexusCipherException;

  /**
   * check if given string is decorated
   * 
   * @param str
   * @return
   */
  public boolean isEncryptedString(
      String str );

  /**
   * return string inside decorations
   * 
   * @param str
   * @return
   * @throws PlexusCipherException
   */
  public String unDecorate(
      String str )
      throws PlexusCipherException;

  /**
   * decorated given string with { and }
   * 
   * @param str
   * @return
   */
  public String decorate(
      String str );

}
