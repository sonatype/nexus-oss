/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.realms.publickey;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * A {@link PublicKey} wrapper which implements {@code equals}, so they can be compared.
 * 
 * @author hugo@josefson.org
 */
class PublicKeyWithEquals
    implements PublicKey
{

    private static final long serialVersionUID = 3668007428213640544L;

    private final PublicKey key;

    /**
     * Constructs this wrapper with the specified key.
     * 
     * @param key the {@link PublicKey} to wrap.
     */
    public PublicKeyWithEquals( PublicKey key )
    {
        this.key = key;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( !( o instanceof PublicKeyWithEquals ) )
            return false;

        PublicKeyWithEquals that = (PublicKeyWithEquals) o;

        final String algorithm = getAlgorithm();
        final String thatAlgorithm = that.getAlgorithm();
        if ( algorithm != null ? !algorithm.equals( thatAlgorithm ) : thatAlgorithm != null )
            return false;

        if ( !Arrays.equals( getEncoded(), that.getEncoded() ) )
            return false;

        final String format = getFormat();
        final String thatFormat = that.getFormat();
        if ( format != null ? !format.equals( thatFormat ) : thatFormat != null )
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        final String algorithm = getAlgorithm();
        final String format = getFormat();
        final byte[] encoded = getEncoded();
        int result = algorithm != null ? algorithm.hashCode() : 0;
        result = 31 * result + ( format != null ? format.hashCode() : 0 );
        result = 31 * result + ( encoded != null ? Arrays.hashCode( encoded ) : 0 );
        return result;
    }

    public String getAlgorithm()
    {
        return key.getAlgorithm();
    }

    public String getFormat()
    {
        return key.getFormat();
    }

    public byte[] getEncoded()
    {
        return key.getEncoded();
    }
}
