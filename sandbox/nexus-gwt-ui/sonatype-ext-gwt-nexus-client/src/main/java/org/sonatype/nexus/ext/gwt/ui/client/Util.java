/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ext.gwt.ui.client;

public class Util {
    
    public static String convertToStyleName(String javaName) {
        StringBuffer styleName = new StringBuffer(javaName.toLowerCase());
        int offset = 0;
        
        for (int i = 1; i < javaName.length(); i++) {
            if (Character.isUpperCase(javaName.charAt(i))) {
                styleName.insert(i + offset, '-');
                offset++;
            }
        }
        
        return styleName.toString();
    }
    
    private static byte[] stringToUTF8(String string) {
        /* First, determine the length of the byte array */
        int length = 0;
        
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            
            if (c < 128) {
                length += 1;
            } else if (c < 2048) {
                length += 2;
            } else if (c < 65536) {
                length += 3;
            } else {
                length += 4;
            }
        }
        
        /* Now convert the string to UTF-8 byte array */
        byte utf8[] = new byte[length];
        
        for (int i = 0, j = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            
            if (c < 128) {
                utf8[j++] = (byte) c;
            } else if (c < 2048) {
                utf8[j++] = (byte) ((c >> 6) | 0xC0);
                utf8[j++] = (byte) ((c & 0x3F) | 0x80);
            } else if (c < 65536) {
                utf8[j++] = (byte) ((c >> 12) | 0xE0);
                utf8[j++] = (byte) (((c >> 6) & 0x3F) | 0x80);
                utf8[j++] = (byte) ((c & 0x3F) | 0x80);
            } else {
                utf8[j++] = (byte) ((c >> 18) | 0xF0);
                utf8[j++] = (byte) (((c >> 12) & 0x3F) | 0x80);
                utf8[j++] = (byte) (((c >> 6) & 0x3F) | 0x80);
                utf8[j++] = (byte) ((c & 0x3F) | 0x80);
            }
        }
        
        return utf8;
    }
    
    private static String base64Characters = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");
    
    public static String base64Encode(String string) {
        StringBuffer encoded = new StringBuffer();
        byte utf8[] = stringToUTF8(string);
        
        int i = 0;
        while (i < utf8.length) {
            byte input1 = utf8[i++];
            byte input2 = i < utf8.length? utf8[i++]: 0;
            byte input3 = i < utf8.length? utf8[i++]: 0;
            
            int output1 = input1 >> 2;
            int output2 = ((input1 & 0x03) << 4) | (input2 >> 4);
            int output3 = ((input2 & 0x0F) << 2) | (input3 >> 6);
            int output4 = input3 & 0x3F;
            
            if (input2 == 0) {
                output3 = output4 = 64;
            } else if (input3 == 0) {
                output4 = 64;
            }
            
            encoded.append(base64Characters.charAt(output1));
            encoded.append(base64Characters.charAt(output2));
            encoded.append(base64Characters.charAt(output3));
            encoded.append(base64Characters.charAt(output4));
        }
        
        return encoded.toString();
    }
    
}
