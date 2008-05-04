/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.applet;


import java.applet.Applet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;

public class DigestApplet extends Applet {
	
  private static final int BUFFER_SIZE = 0x1000;
  private static final String SHA1 = "SHA1";
  private static final Color BACKGROUND_COLOR = new Color( 242, 242, 242 ); 
  
  private long totalBytes = 0L;
  private long currentBytes = 0L;

  public void init() {
    setLayout( new FlowLayout() );
  }
  
  public void paint( Graphics g ) {
    int w = 0;
    Dimension d = getSize();
    if ( totalBytes > 0L ) {
      w = ( int ) ( currentBytes * d.width / totalBytes );
      g.setColor( Color.BLACK );
      g.fillRect( 0, 0, w, d.height );
    }
    g.setColor( BACKGROUND_COLOR );
    g.fillRect( w, 0, d.width - w, d.height );
  }

  public void resetProgress() {
    totalBytes = 0L;
    repaint();
  }
  
  public String digest( final String filename ) {
    return String.valueOf( AccessController.doPrivileged( new PrivilegedAction() {
      public Object run() {
        FileInputStream in = null;
        try {
          currentBytes = 0L;
          totalBytes = new File( filename ).length();
          return readAndDigest( in = new FileInputStream( filename ) );
        }
        catch ( FileNotFoundException fileNotFoundException ) {
          return fileNotFoundException.getMessage();
        }
        catch ( IOException ioException ) {
          ioException.printStackTrace();
          return ioException.getMessage();
        }
        finally {
          if ( in != null ) try {
            in.close();
          }
          catch ( IOException ioException ) {
            ioException.printStackTrace();
            return ioException.getMessage();
          }
        }
      }
    } ) );
  }


  private String readAndDigest( InputStream in ) throws IOException {

  	byte[] bytes = new byte[BUFFER_SIZE];
  	
  	try {
      MessageDigest digest = MessageDigest.getInstance( SHA1 );
      for ( int n; ( n = in.read( bytes ) ) >= 0; ) {
        if ( n > 0 ) {
          digest.update( bytes, 0, n );
          currentBytes += n;
          repaint();
        }
      }
      
      bytes = digest.digest();
      StringBuffer sb = new StringBuffer( bytes.length * 2 );
      for ( int i = 0; i < bytes.length; i++ ) {
        int n = bytes[i] & 0xFF;
        if ( n < 0x10 ) {
          sb.append( '0' );
        }
        sb.append( Integer.toHexString( n ) );
      }
      
      return sb.toString();
    }
  	catch ( NoSuchAlgorithmException noSuchAlgorithmException ) {
  	  noSuchAlgorithmException.printStackTrace();
  	  return noSuchAlgorithmException.getMessage();
  	}
  }
}
