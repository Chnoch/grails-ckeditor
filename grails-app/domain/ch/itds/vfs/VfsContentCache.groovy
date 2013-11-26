package ch.itds.vfs

class VfsContentCache {
    
    static belongsto = [file:VfsFile]
    String key
    byte[] content
    int csize
    String type

    static constraints = {
         /* max 10 mb */
           content(size: 0..10000000)
    }
}
