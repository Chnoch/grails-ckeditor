package ch.itds.vfs

import ch.itds.vfs.VfsFile
import ch.itds.vfs.VfsFolder

/**
 *  Contains settings and callback for the vfs. Can be overwritten by the application in the config.
 */
public class VfsConfiguration {

    enum ACCESS_TYPE {
        READ, WRITE
    }

    public boolean checkAccess(VfsItem item, ACCESS_TYPE type) {//TODO: type zu enum machen
        return true;
    }
    /**
     * Can be overwritten to atomatically generate the file saving
     */
    public VfsFolder generateParent(VfsFile f) {
        return null
    }
}

