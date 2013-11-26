package ch.itds.vfs

class VfsFolder extends VfsItem{

    static constraints = {
        name(nullable:false, empty:false)
    }
   
    String toString() {
        url;
    }
	
	public VfsFolder() {
		ctype="folder"
	}
    
}
