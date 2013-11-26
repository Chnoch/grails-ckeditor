package ch.itds.vfs

import ch.itds.vfs.VfsContent
import ch.itds.vfs.VfsContentCache
import ch.itds.vfs.VfsFile
import ch.itds.vfs.VfsFolder
import ch.itds.vfs.VfsItem
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.awt.*
import java.awt.image.renderable.ParameterBlock

//import javax.media.jai.BorderExtender
//import javax.media.jai.InterpolationBicubic
//import javax.media.jai.InterpolationBicubic2
//import javax.media.jai.InterpolationBilinear
//import javax.media.jai.OpImage
//import javax.media.jai.RenderedOp
//
//import com.sun.media.jai.codec.ImageEncoder
//import com.sun.media.jai.codec.JPEGEncodeParam
//import com.sun.media.jai.codec.SeekableStream
//
//import javax.media.jai.JAI;

class VfsService {
//    VfsConfiguration conf

    def grailsApplication

    public VfsService() {
        try {
            def myConf = ApplicationHolder.application.config.vfs.configuration
            conf = myConf.newInstance()
        } catch (Exception e) {
//            conf = new VfsConfiguration()
        }
    }


    public VfsFolder generateParent(VfsFile f) {
        return null
//        return conf.generateParent(f)
    }

    static scope = "session"//1 Service pro session, erlaubt speichern von
    // session spezifischen Daten direkt im Service (Applikation usw...)

    static transactional = true

    /* Folder with ID 0 is hardcoded to be the VFS root
    */

    VfsFolder getVfsRoot() {
        //new VfsFolder(0)
        //TODO: create if it doesn't exist

        return getVfsRoot(grailsApplication.metadata['app.name']);

    }

    VfsFolder getVfsRoot(module) {
        if (log.isDebugEnabled())
            log.debug("ask for VfsRoot(module=${module})");
        def root = VfsFolder.findOrCreateWhere(name: module, parent: null, ctype: 'vfs-itds/directory').save(failOnError: true);
        return root
    }


    def createVfsFileFromMultipartFile(CommonsMultipartFile o) {
        VfsFile f = new VfsFile()
        f.ctype = o.contentType
        f.csize = o.size
        f.name = o.originalFilename
        //f.parameterName = o.name
        f.content = new VfsContent()
        f.content.content = o.bytes
        f.generateParent()
        if (f.name == "") {
            f = null
        }
        return f
    }

    /* Creates file from multipart file (File upload)
    */

    public VfsFile createFile(VfsFolder parent, CommonsMultipartFile input) {
        VfsFile f = new VfsFile()
        f.parent = parent
        f.name = input.originalFilename
        f.csize = input.size
        f.ctype = input.contentType
        f.content = new VfsContent(content: input.bytes)
        return f
    }

    /* Creates file from an URL (File upload)
    */

    public VfsFile createFileFromUrl(VfsFolder parent, String url, String name) {
        URL urlObject = new URL(url);

        def connection = urlObject.openConnection()
        println connection.getResponseCode()
        if (connection.getResponseCode() != 200) {
            return null
        }
        println connection.getResponseMessage()
        byte[] bytes = IOUtils.toByteArray(connection.getInputStream());

        VfsFile f = new VfsFile()
        f.parent = parent
        f.name = name
        f.csize = bytes.length
        f.ctype = connection.getContentType()
        f.content = new VfsContent(content: bytes)
        return f

    }

    def checkAccess(item, type) {

//        if (conf.checkAccess)
//            conf.checkAccess(item, type)
//        else
            true
    }


    def deleteItem(item) {

        if (item instanceof VfsFolder) {
            def children = VfsItem.findAllByParent(item)
            children.each { child ->
                deleteItem(child)
            }
        }
        item.delete();

    }

    def moveTo(item, dst) {
        def nitem = copyTo(item, dst);
        deleteItem(item)
        return nitem
    }

    def copyTo(item, dst) {
        def nitem = null;
        if (item instanceof VfsFile) {
            println "copy item=${item}"
            nitem = new VfsFile(name: item.name, parent: dst, ctype: item.ctype, csize: item.csize)
            println "file created"
            def data = item.content.content;
            def nc = new VfsContent(content: data)
            println "content created"
            nitem.content = nc;
            println "content assigned"
            nc.save(failOnError: true)
            println "content saved"
            nitem.save(failOnError: true)
            println "file saved"
        } else if (item instanceof VfsFolder) {

            nitem = new VfsFolder(name: item.name, parent: dst, ctype: item.ctype).save(failOnError: true)

            def children = VfsItem.findAllByParent(item)
            children.each { c ->
                copyTo(c, nitem)
            }
        }
        return nitem;
    }

//   def generateResized(VfsFile vfsFileInstance, width, height) {
//	   def keySource = "file=${vfsFileInstance.id};resizeTo=${width}x${height}";
//	   def key = keySource.encodeAsMD5();
//
//	   // Hole File + ContentCache Liste ( Doppeltes Array! )
//	   def vfsFileContentCacheInstanceList = VfsContentCache.findAll("from VfsFile f left join  f.contentCache as cc WHERE f.id = ? AND cc.key = ? ",[vfsFileInstance.id,key]);
//
//	   // Nimm ContentCache vom ersten Eintrag, d.h. [0][1] ; [0][0] wäre das VfsFile
//	   def vfsContentCacheInstance = (vfsFileContentCacheInstanceList.size()==0)?null:vfsFileContentCacheInstanceList[0][1];
//
//
//	   if ( !vfsContentCacheInstance ) {
//		   ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//			   /*println "resizeTo ${width}x${height}"
//			   println "resize ${vfsFileInstance}"*/
//
//			   def vfsContentInstance = VfsContent.get(vfsFileInstance.content.id);
//
//			   def type = resize(vfsContentInstance.content,stream,width,height)
//			   vfsContentCacheInstance = new VfsContentCache()
//			   //vfsContentCacheInstance.file = vfsFileInstance
//			   vfsContentCacheInstance.content = stream.toByteArray()
//			   vfsContentCacheInstance.csize = vfsContentCacheInstance.content.length
//			   vfsContentCacheInstance.key = key
//			   vfsContentCacheInstance.type = type
//			   vfsContentCacheInstance.save(flush:true,failOnError:true);
//
//			   vfsFileInstance.addToContentCache(vfsContentCacheInstance)
//
//			   /*println " VfsFileItem ${vfsFileInstance.id}"*/
//
//
//			   vfsFileInstance.save(flush:true,failOnError:true);
//
//	   }
//	   return
//	   //return vfsContentCacheInstance.content
//   }
//
//	/**
//	 * This method creates a thumbnail of the maxWidth and maxHeight it takes as a parameter
//	 *
//	 * Example : Calling the method thumnailSpecial(640, 480, 1, 1)
//	 * will never produce images larger than 640 on the width, and never larger than 480 on the height and use
//	 * InterpolationBilinear(8) and scale
//	 *
//	 * @param maxWidth
//	 * The maximum width the thumbnail is allowed to have
//	 *
//	 * @param maxHeigth
//	 * The maximum height the thumbnail is allowed to have
//	 *
//	 * @param interPolationType
//	 * Is for you to choose what interpolation you wish to use
//	 * 1 : InterpolationBilinear(8) // Produces good image quality with smaller image size(byte) then the other two
//	 * 2 : InterpolationBicubic(8) // Supposed to produce better than above, but also larger size(byte)
//	 * 3 : InterpolationBicubic2(8) // Supposed to produce the best of the three, but also largest size(byte)
//	 *
//	 * @param renderingType
//	 * Too choose the rendering type
//	 * 1: Uses scale // Better on larger thumbnails
//	 * 2: Uses SubsampleAverage // Produces clearer images when it comes to really small thumbnail e.g 80x60
//	 */
//	static RenderedOp thumbnailSpecial(RenderedOp image, float maxWidth, float maxHeight, int interPolationType, int renderingType) {
//		def width = image.getWidth()
//		def height = image.getHeight()
//		RenderedOp result
//		if (height <= maxHeight && width <= maxWidth) {
//			/* Don't change, keep it as it is, even though one might loose out on the compression included below (not sure)*/
//			result = image
//		}
//		else {
//			boolean tall = (height * (maxWidth / maxHeight) > width);
//			double modifier = maxWidth / (float) (tall ? (height * (maxWidth / maxHeight)) : width);
//			ParameterBlock params = new ParameterBlock();
//			params.addSource(image);
//
//			// We had to do this because of that the different rendering
//			// options require either float or double. This ended up having
//			// a side effect of Java apparently not correctly converting from
//			// floats to double, so we have to keep the value as a double
//			// and then convert to float when necessary
//			switch (renderingType) {
//				case 1:
//					params.add((float)modifier);//x scale factor
//					params.add((float)modifier);//y scale factor
//					break;
//				case 2:
//					params.add(modifier);//x scale factor
//					params.add(modifier);//y scale factor
//					break;
//				default:
//					params.add((float)modifier);//x scale factor
//					params.add((float)modifier);//y scale factor
//					break;
//			}
//
//			params.add(0.0F);//x translate
//			params.add(0.0F);//y translate
//			switch (interPolationType) {
//				case 1: params.add(new InterpolationBilinear(8)); break; // Produces good image quality with smaller image size(byte) then the other two
//				case 2: params.add(new InterpolationBicubic(8)); break; // Supposed to produce better than above, but also larger size(byte)
//				case 3: params.add(new InterpolationBicubic2(8)); break; // Supposed to produce the best of the two, but also largest size(byte)
//				default: params.add(new InterpolationBilinear(8)); break;
//			}
//
//
//			switch (renderingType) {
//				case 1:
//					RenderingHints qualityHints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));
//					result = JAI.create("scale", params, qualityHints); break;
//				case 2:
//					RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//
//						result = JAI.create("SubsampleAverage", params, qualityHints); break;
//				default: result = JAI.create("scale", params); break;
//			}
//		}
//		return result
//	}
//
//   static resize = { bytes, out, maxW, maxH ->
//	   RenderedOp objImage = null;
//	   InputStream is = new ByteArrayInputStream(bytes);
//	   SeekableStream s = SeekableStream.wrapInputStream(is, true);
//
//	   objImage = JAI.create("stream", s);
//	   int renderType = 2
//	   if(maxH > 200 && maxW > 200)
//	   		renderType = 1
//   	   RenderedOp nobjImage = thumbnailSpecial(objImage, (float) maxW, (float) maxH, 3,renderType)
//   /*
//   ((OpImage)objImage.getRendering()).setTileCache(null);
//
//	   double xScale = ((double)maxW)/(objImage.getWidth());
//	   double yScale = ((double)maxH)/(objImage.getHeight());
//
//	   if ( xScale > yScale )
//	   {
//			   xScale = yScale;
//	   } else {
//			   yScale = xScale;
//	   }
//
//	   if ( xScale > 1 ) xScale = 1;
//	   if ( yScale > 1 ) yScale = 1;
//	   Map<RenderingHints.Key, Object> map = new HashMap<RenderingHints.Key, Object>();
//	   map.put(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//	   map.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
//	   map.put(RenderingHints.KEY_COLOR_RENDERING,  RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//	   map.put(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
//	   map.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
//	   map.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//	   RenderingHints hints = new RenderingHints(map);
//
//	   //println " xScale=${xScale} + yScale=${yScale} : ${objImage}"
//
//	   def nobjImage =  JAI.create("SubsampleAverage",
//			   objImage, xScale, yScale, hints);
//	   */
//	   def type = "jpg"
//
//	   try {
//		   FileOutputStream fout = new FileOutputStream(file);
//		   JPEGEncodeParam encodeParam = new JPEGEncodeParam();
//		   encodeParam.setQuality(0.92f); //My experience is anything below 0.92f gives bad result
//		   ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG", out, encodeParam);
//		   encoder.encode(nobjImage);
//	   } catch ( Exception e  )
//	   {
//		   JAI.create("encode", nobjImage, out, "PNG", null);
//		   type = "png"
//	   }
//	   return type
//
//   }
//
//   def generateCropped(VfsFile vfsFileInstance,  offsetX, offsetY, width, height) {
//	   def keySource = "file=${vfsFileInstance.id};cropTo=${width}x${height};offset=${offsetX},${offsetY}";
//	   def key = keySource.encodeAsMD5();
//
//	   // Hole File + ContentCache Liste ( Doppeltes Array! )
//	   def vfsFileContentCacheInstanceList = VfsContentCache.findAll("from VfsFile f left join  f.contentCache as cc WHERE f.id = ? AND cc.key = ? ",[vfsFileInstance.id,key]);
//
//	   // Nimm ContentCache vom ersten Eintrag, d.h. [0][1] ; [0][0] wäre das VfsFile
//	   def vfsContentCacheInstance = (vfsFileContentCacheInstanceList.size()==0)?null:vfsFileContentCacheInstanceList[0][1];
//
//
//	   if ( !vfsContentCacheInstance ) {
//		   ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//			   /*println "resizeTo ${width}x${height}"
//			   println "resize ${vfsFileInstance}"*/
//
//			   def vfsContentInstance = VfsContent.get(vfsFileInstance.content.id);
//
//			   def type = crop(vfsContentInstance.content,stream, offsetX, offsetY,width,height)
//			   vfsContentCacheInstance = new VfsContentCache()
//			   //vfsContentCacheInstance.file = vfsFileInstance
//			   vfsContentCacheInstance.content = stream.toByteArray()
//			   vfsContentCacheInstance.csize = vfsContentCacheInstance.content.length
//			   vfsContentCacheInstance.key = key
//			   vfsContentCacheInstance.type = type
//			   vfsContentCacheInstance.save(flush:true,failOnError:true);
//
//			   vfsFileInstance.addToContentCache(vfsContentCacheInstance)
//
//			   /*println " VfsFileItem ${vfsFileInstance.id}"*/
//
//
//			   vfsFileInstance.save(flush:true,failOnError:true);
//
//	   }
//	   return
//	   //return vfsContentCacheInstance.content
//   }
//
//   static crop = { bytes, out, offsetX, offsetY, maxW, maxH ->
//	   RenderedOp objImage = null;
//	   InputStream is = new ByteArrayInputStream(bytes);
//	   SeekableStream s = SeekableStream.wrapInputStream(is, true);
//
//	   objImage = JAI.create("stream", s);
//	   def type = "jpg"
//	   def pb = new ParameterBlock();
//	   pb.addSource(objImage);
//	   pb.add((float) offsetX);
//	   pb.add((float) offsetY);
//	   pb.add((float) maxW);
//	   pb.add((float) maxH);
//	   def nobjImage = JAI.create("crop",pb);
//
//	   try {
//		   FileOutputStream fout = new FileOutputStream(file);
//		   JPEGEncodeParam encodeParam = new JPEGEncodeParam();
//		   encodeParam.setQuality(0.92f); //My experience is anything below 0.92f gives bad result
//		   ImageEncoder encoder = ImageCodec.createImageEncoder("JPEG", out, encodeParam);
//		   encoder.encode(nobjImage);
//	   } catch ( Exception e  )
//	   {
//		   JAI.create("encode", nobjImage, out, "PNG", null);
//		   type = "png"
//	   }
//	   return type
//
//   }

}