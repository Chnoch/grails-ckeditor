package ch.itds.vfs

class VfsContent {
    
    byte[] content
    static belongsTo = [VfsFile]

    static constraints = {
        /* max 800 mb */
        content(size: 0..800000000)
    }
}
