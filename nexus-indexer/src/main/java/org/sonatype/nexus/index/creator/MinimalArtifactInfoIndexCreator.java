/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.model.Model;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.ArtifactAvailablility;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.locator.JavadocLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.Sha1Locator;
import org.sonatype.nexus.index.locator.SignatureLocator;
import org.sonatype.nexus.index.locator.SourcesLocator;

/**
 * A minimal index creator used to provide basic information about Maven artifact.
 */
@Component( role = IndexCreator.class, hint = MinimalArtifactInfoIndexCreator.ID )
public class MinimalArtifactInfoIndexCreator
    extends AbstractIndexCreator
    implements LegacyDocumentUpdater
{
    public static final String ID = "min";

    private Locator jl = new JavadocLocator();

    private Locator sl = new SourcesLocator();

    private Locator sigl = new SignatureLocator();

    private Locator sha1l = new Sha1Locator();

    public void populateArtifactInfo( ArtifactContext ac )
    {
        File artifact = ac.getArtifact();

        File pom = ac.getPom();

        ArtifactInfo ai = ac.getArtifactInfo();

        if ( pom != null )
        {
            ai.lastModified = pom.lastModified();

            ai.fextension = "pom";
        }

        // TODO handle artifacts without poms
        if ( pom != null )
        {
            if ( ai.classifier != null )
            {
                ai.sourcesExists = ArtifactAvailablility.NOT_AVAILABLE;

                ai.javadocExists = ArtifactAvailablility.NOT_AVAILABLE;
            }
            else
            {
                File sources = sl.locate( pom );
                if ( !sources.exists() )
                {
                    ai.sourcesExists = ArtifactAvailablility.NOT_PRESENT;
                }
                else
                {
                    ai.sourcesExists = ArtifactAvailablility.PRESENT;
                }

                File javadoc = jl.locate( pom );
                if ( !javadoc.exists() )
                {
                    ai.javadocExists = ArtifactAvailablility.NOT_PRESENT;
                }
                else
                {
                    ai.javadocExists = ArtifactAvailablility.PRESENT;
                }
            }
        }

        if ( artifact != null )
        {
            File signature = sigl.locate( artifact );
            
            ai.signatureExists = signature.exists() ? ArtifactAvailablility.PRESENT : ArtifactAvailablility.NOT_PRESENT;

            File sha1 = sha1l.locate( artifact );

            if ( sha1.exists() )
            {
                try
                {
                    ai.sha1 = StringUtils.chomp( FileUtils.fileRead( sha1 ) ).trim().split( " " )[0];
                }
                catch ( IOException e )
                {
                    ac.addError( e );
                }
            }

            ai.lastModified = artifact.lastModified();

            ai.size = artifact.length();

            ai.fextension = getExtension( artifact, ac.getGav() );

            if ( ai.packaging == null )
            {
                ai.packaging = ai.fextension;
            }
        }

        Model model = ac.getPomModel();

        if ( model != null )
        {
            ai.name = model.getName();

            ai.description = model.getDescription();

            if ( model.getPackaging() != null && ai.classifier == null )
            {
                // only when this is not a classified artifact
                ai.packaging = model.getPackaging();
            }
        }
    }

    private String getExtension( File artifact, Gav gav )
    {
        if ( gav != null && StringUtils.isNotBlank( gav.getExtension() ) )
        {
            return gav.getExtension();
        }

        // last resort, the extension of the file
        String artifactFileName = artifact.getName().toLowerCase();

        // tar.gz? and other "special" combinations
        if ( artifactFileName.endsWith( "tar.gz" ) )
        {
            return "tar.gz";
        }
        else if ( artifactFileName.equals( "tar.bz2" ) )
        {
            return "tar.bz2";
        }

        // get the part after the last dot
        return FileUtils.getExtension( artifactFileName );
    }

    public void updateDocument( ArtifactInfo ai, Document doc )
    {
        String info =
            new StringBuilder().append( ai.packaging ).append( ArtifactInfo.FS ).append(
                Long.toString( ai.lastModified ) ).append( ArtifactInfo.FS ).append( Long.toString( ai.size ) ).append(
                ArtifactInfo.FS ).append( ai.sourcesExists.toString() ).append( ArtifactInfo.FS ).append(
                ai.javadocExists.toString() ).append( ArtifactInfo.FS ).append( ai.signatureExists.toString() ).append(
                ArtifactInfo.FS ).append( ai.fextension ).toString();

        doc.add( new Field( ArtifactInfo.INFO, info, Field.Store.YES, Field.Index.NO ) );

        doc.add( new Field( ArtifactInfo.GROUP_ID, ai.groupId, Field.Store.NO, Field.Index.TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.ARTIFACT_ID, ai.artifactId, Field.Store.NO, Field.Index.TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.VERSION, ai.version, Field.Store.NO, Field.Index.TOKENIZED ) );

        if ( ai.name != null )
        {
            doc.add( new Field( ArtifactInfo.NAME, ai.name, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.description != null )
        {
            doc.add( new Field( ArtifactInfo.DESCRIPTION, ai.description, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.packaging != null )
        {
            doc.add( new Field( ArtifactInfo.PACKAGING, ai.packaging, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
        }

        if ( ai.classifier != null )
        {
            doc.add( new Field( ArtifactInfo.CLASSIFIER, ai.classifier, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
        }

        if ( ai.sha1 != null )
        {
            doc.add( new Field( ArtifactInfo.SHA1, ai.sha1, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }
    }

    public void updateLegacyDocument( ArtifactInfo ai, Document doc )
    {
        updateDocument( ai, doc );

        // legacy!
        if ( ai.prefix != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_PREFIX, ai.prefix, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }

        if ( ai.goals != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_GOALS, ArtifactInfo.lst2str( ai.goals ), Field.Store.YES,
                Field.Index.NO ) );
        }

        doc.removeField( ArtifactInfo.GROUP_ID );
        doc.add( new Field( ArtifactInfo.GROUP_ID, ai.groupId, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
    }

    public boolean updateArtifactInfo( Document doc, ArtifactInfo ai )
    {
        boolean res = false;

        String uinfo = doc.get( ArtifactInfo.UINFO );

        if ( uinfo != null )
        {
            String[] r = ArtifactInfo.FS_PATTERN.split( uinfo );

            ai.groupId = r[0];

            ai.artifactId = r[1];

            ai.version = r[2];

            if ( r.length > 3 )
            {
                ai.classifier = ArtifactInfo.renvl( r[3] );
            }

            res = true;
        }

        String info = doc.get( ArtifactInfo.INFO );

        if ( info != null )
        {
            String[] r = ArtifactInfo.FS_PATTERN.split( info );

            ai.packaging = r[0];

            ai.lastModified = Long.parseLong( r[1] );

            ai.size = Long.parseLong( r[2] );

            ai.sourcesExists = ArtifactAvailablility.fromString( r[3] );

            ai.javadocExists = ArtifactAvailablility.fromString( r[4] );

            ai.signatureExists = ArtifactAvailablility.fromString( r[5] );

            if ( r.length > 6 )
            {
                ai.fextension = r[6];
            }
            else
            {
                if ( ai.classifier != null //
                    || "pom".equals( ai.packaging ) //
                    || "war".equals( ai.packaging ) //
                    || "ear".equals( ai.packaging ) )
                {
                    ai.fextension = ai.packaging;
                }
                else
                {
                    ai.fextension = "jar"; // best guess
                }
            }

            res = true;
        }

        String name = doc.get( ArtifactInfo.NAME );

        if ( name != null )
        {
            ai.name = name;

            res = true;
        }

        String description = doc.get( ArtifactInfo.DESCRIPTION );

        if ( description != null )
        {
            ai.description = description;

            res = true;
        }

        // sometimes there's a pom without packaging(default to jar), but no artifact, then the value will be a "null"
        // String
        if ( "null".equals( ai.packaging ) )
        {
            ai.packaging = null;
        }

        String sha1 = doc.get( ArtifactInfo.SHA1 );

        if ( sha1 != null )
        {
            ai.sha1 = sha1;
        }

        return res;

        // artifactInfo.fname = ???
    }

    // ==

    @Override
    public String toString()
    {
        return ID;
    }
}
