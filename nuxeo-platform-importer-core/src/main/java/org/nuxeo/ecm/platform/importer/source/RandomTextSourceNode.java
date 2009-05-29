package org.nuxeo.ecm.platform.importer.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.platform.importer.random.RandomTextGenerator;

public class RandomTextSourceNode implements SourceNode {

    protected static RandomTextGenerator gen;

    protected static int maxNode = 10000;

    public static int maxDepth = 8;
    public static int defaultNbDataNodesPerFolder = 15;

    protected static int minGlobalFolders=0;
    protected static int minFoldersPerNode = 0;

    protected static Integer nbNodes = 1;
    protected static Integer nbFolders = 0;

    protected static Long size;

    protected Random hazard;

    protected String name;

    protected boolean folderish;

    protected int level=0;

    protected int idx=0;

    protected static Integer blobSizeInKB;

    protected List<SourceNode> cachedChildren = null;

    public static boolean CACHE_CHILDREN=false;

    public RandomTextSourceNode(boolean folderish, int level, int idx) {
        this.folderish = folderish;
        hazard = new Random(System.currentTimeMillis());
        this.level=level;
        this.idx=idx;

    }

    public static RandomTextSourceNode init(int maxSize) throws Exception {
        return init(maxSize, null);
    }


    public static RandomTextSourceNode init(int maxSize, Integer blobSizeInKB) throws Exception {
        gen = new RandomTextGenerator();
        gen.prefilCache();
        maxNode = maxSize;
        nbNodes = 1;
        size = new Long(0);
        RandomTextSourceNode.blobSizeInKB = blobSizeInKB;
        minGlobalFolders = maxNode / defaultNbDataNodesPerFolder;
        minFoldersPerNode = 1 + (int) Math.pow(minGlobalFolders, (1.0/maxDepth));
        return new RandomTextSourceNode(true, 0, 0);
    }

    public BlobHolder getBlobHolder() {
        if (folderish) {
            return null;
        }
        String content = null;

        if (blobSizeInKB==null) {
            content = gen.getRandomText();
        } else {
            content = gen.getRandomText(blobSizeInKB);
        }
        synchronized (size) {
            size += content.length();
        }
        Blob blob = new StringBlob(content);
        blob.setFilename(getName() + ".txt");
        blob.setMimeType("text/plain");
        return new SimpleBlobHolder(blob);
    }



    protected int getMidRandom(int target) {
        return 1 + (target/2)+ hazard.nextInt(target);
    }

    protected int getMaxChildren() {
        if (maxNode < nbNodes) {
            return 0;
        }
        int targetRemainingFolders=minGlobalFolders - nbFolders;
        if (targetRemainingFolders<=0) {
            return defaultNbDataNodesPerFolder+1;
        }
        int target = ((maxNode - nbNodes) / targetRemainingFolders);
        if (target <=0) {
            return 0;
        }
        return getMidRandom(target);
    }
    protected int getMaxFolderish() {
        if (maxNode <= nbNodes) {
            return 0;
        }
        return getMidRandom(minFoldersPerNode);
    }

    public List<SourceNode> getChildren() {

        if (!folderish) {
            return null;
        }

        if (this.cachedChildren!=null) {
            return this.cachedChildren;
        }

        List<SourceNode> children = new ArrayList<SourceNode>();
        if (nbNodes > maxNode) {
            return children;
        }

        int nbChildren = getMaxChildren();

        synchronized (nbNodes) {
            nbNodes = nbNodes + nbChildren;
        }
        for (int i = 0; i< nbChildren; i++) {
            children.add(new RandomTextSourceNode(false,level, i));
        }
        if (level < maxDepth) {
            int nbFolderish = getMaxFolderish();
            for (int i = 0; i< nbFolderish; i++) {
                children.add(new RandomTextSourceNode(true, level+1, i));
            }
            synchronized (nbFolders) {
                nbFolders = nbFolders + nbFolderish;
            }
        }
        if (CACHE_CHILDREN) {
            this.cachedChildren = children;
        }
        return children;
    }

    public String getName() {
        if (name==null) {
            if (folderish) {
                name = "folder";
            } else {
                name = "file";
            }
            if (level==0 && folderish) {
                name = name + "-" + (System.currentTimeMillis()%10000) + hazard.nextInt(100);
            }
            else {
                name = name + "-" + level + "-" + idx;
            }
        }
        return name;
    }

    public boolean isFolderish() {
        return folderish;
    }

    public static Integer getNbNodes() {
        return nbNodes;
    }

    public static Long getSize() {
        return size;
    }

    public int getLevel() {
        return level;
    }

}
