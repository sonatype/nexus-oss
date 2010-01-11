package org.sonatype.nexus.plugins.rrb;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.mapper.MapperException;
import com.sdicons.json.model.JSONValue;

public class RepositoryDirectory {
    private String resourceURI;
    private String relativePath;
    private String text;
    private boolean leaf;
    private String lastModified;
    private int sizeOnDisk = -1;

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String baseUrl) {
        this.resourceURI = baseUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String name) {
        this.text = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resourceURI == null) ? 0 : resourceURI.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryDirectory other = (RepositoryDirectory) obj;
        if (resourceURI == null) {
            if (other.resourceURI != null) {
                return false;
            }
        } else if (!resourceURI.equals(other.resourceURI)) {
            return false;
        }
        return true;
    }

    public String toJSON() {
        JSONValue value = null;
        try {
            value = JSONMapper.toJSON(this);
        } catch (MapperException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value.render(true);
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public int getSizeOnDisk() {
        return sizeOnDisk;
    }

    public void setSizeOnDisk(int sizeOnDisk) {
        this.sizeOnDisk = sizeOnDisk;
    }
}