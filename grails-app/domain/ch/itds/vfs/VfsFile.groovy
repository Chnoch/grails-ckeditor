package ch.itds.vfs

import ch.itds.vfs.VfsService

class VfsFile extends VfsItem {

	long csize

	VfsContent content

	static hasMany = [contentCache : VfsContentCache]

	static constraints = {
		name(nullable:false, empty:false)
		content(nullable:true)
	}

	def generateParent = {
		println("vf generateParent")
		VfsService s = new VfsService()
		parent = s.generateParent()
		//Standard parent /
		//Objekt hat dir? -> /DIR/var/name
		//sonst: /INSTANCE/OBJID/var/name
	}
	/*def beforeValidate = {//beforeValidate erst ab grails 2.0
	 println("beforeCreate")
	 if(parent == null) {
	 parent = generateParent()
	 }
	 }*/

	public String toString() {
		name + " " + csize +"B"
	}
}
