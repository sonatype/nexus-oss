package org.sonatype.nexus.plugin.migration.artifactory.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "repositoryResolution" )
public class RepositoryResolutionDTO
{

    private String repositoryId;

    private String type;

    private boolean mapUrls = true;

    private boolean copyCachedArtifacts = true;

    private boolean isMixed = false;

    private String mixResolution = EMixResolution.BOTH.name();

    private String similarRepository;

    private boolean mergeSimilarRepository = false;


    public RepositoryResolutionDTO()
    {
        super();
    }

    public RepositoryResolutionDTO( String repositoryId, ERepositoryType type, String similarRepository )
    {
        this();
        this.repositoryId = repositoryId;
        this.type = type.name();
        this.similarRepository = similarRepository;
    }

    public EMixResolution getMixResolution()
    {
        return EMixResolution.valueOf( mixResolution );
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public ERepositoryType getType()
    {
        return ERepositoryType.valueOf( this.type );
    }

    public boolean isCopyCachedArtifacts()
    {
        return copyCachedArtifacts;
    }

    public boolean isMapUrls()
    {
        return mapUrls;
    }

    public boolean isMixed()
    {
        return isMixed;
    }

    public void setCopyCachedArtifacts( boolean copyCachedArtifacts )
    {
        this.copyCachedArtifacts = copyCachedArtifacts;
    }

    public void setMapUrls( boolean mapUrls )
    {
        this.mapUrls = mapUrls;
    }

    public void setMixed( boolean isMixed )
    {
        this.isMixed = isMixed;
    }

    public void setMixResolution( EMixResolution mixResolution )
    {
        this.mixResolution = mixResolution.name();
    }

    public void setRepositoryId( String repositoryId )
    {
        this.repositoryId = repositoryId;
    }

    public void setType( ERepositoryType type )
    {
        this.type = type.name();
    }

    public String getSimilarRepository()
    {
        return similarRepository;
    }

    public void setSimilarRepository( String similarRepository )
    {
        this.similarRepository = similarRepository;
    }

    public boolean isMergeSimilarRepository()
    {
        return mergeSimilarRepository;
    }

    public void setMergeSimilarRepository( boolean mergeSimilarRepository )
    {
        this.mergeSimilarRepository = mergeSimilarRepository;
    }

}
