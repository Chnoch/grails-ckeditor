package ch.itds.vfs

class VfsItem {
    
    String name
    VfsFolder parent
    String ctype
    String url
    Date tcreated = new Date()
    Date tmodified = new Date()
    Date taccess = new Date()
    

    
    static constraints = {
        parent(nullable:true)
        name(nullable:false, empty:false)
    }
    
    static mapping = {
        tablePerHierarchy true
    }
    
    String toString() {
        url;
    }
    
    
    def beforeValidate() {
        if ( !tcreated )
            tcreated = new Date();
        if ( !tmodified )
            tmodified = new Date();
        if ( !taccess )
            taccess = new Date();
        url = generateUrl();    
    }
    
    def beforeInsert() {
       tcreated = new Date()
       url = generateUrl();
   }
   def beforeUpdate() {
       tmodified = new Date()
       taccess = new Date()
       url = generateUrl();
   } 
   def afterUpdate() {
       if ( this instanceof VfsFolder )
       {
           def fi = VfsFolder.get(this.id);
           //println "update children of ${fi.id}"
           VfsItem.withNewSession { session ->
               def children = VfsItem.findAllByParent(fi)
               children.each { child ->
                   //println "update child: ${child} (${child.version})"
                   child.save(failOnError:true,flush:true);
                   //println "new child url="+child.url+"  (${child.version})";
               }
           }
       }
        
   }
   def generateUrl()
   {
        def nurl = "/"+name
        if ( parent )
            nurl = parent.url+nurl
        return nurl    
   }
    
    
}
