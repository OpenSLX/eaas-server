package de.bwl.bwfla.imagearchive;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

import de.bwl.bwfla.common.services.net.HttpExportServlet;
import de.bwl.bwfla.imagearchive.datatypes.ImageArchiveMetadata.ImageType;

import javax.inject.Inject;


public class ExportObject extends HttpExportServlet
{
	@Inject
	private ImageArchiveRegistry backends = null;

	private File resolveFile(String imageArchiveName, String filename)
	{
		final ImageArchiveBackend backend = backends.lookup(imageArchiveName);
		if(backend == null) {
			log.severe("backend for " + imageArchiveName + " not found");
			return null;
		}
		for (ImageType type : ImageType.values()) {
			File f = new File(backend.getConfig().getImagePath() + "/" + type.name() + filename);
			if (f.exists())
				return f;
		}
		return null;
	}

	@Override
	public File resolveRequest(String reqStr) 
	{
		String imageArchiveName = ExportObject.parseImageArchiveName(reqStr);
		String filename = reqStr.substring(imageArchiveName.length() + 1);
		try {
			imageArchiveName = URLDecoder.decode(imageArchiveName, "UTF-8");
			filename = URLDecoder.decode(filename, "UTF-8");
		}
		catch (UnsupportedEncodingException error) {
			log.log(Level.WARNING, error.getMessage(), error);
			return null;
		}

		File image = resolveFile(imageArchiveName, filename);
		// TODO: temporary hack to avoid copying an image file to the private/default archive if only the metadata is updated.
		if(image == null) {
			if (imageArchiveName.equals("default"))
				return resolveFile("public", filename);
			else if(imageArchiveName.equals("public"))
				return resolveFile("default", filename);
		}
		return image;
	}

	private static String parseImageArchiveName(String url)
	{
		// Expected URL:  /{archive-name}/{image-id}
		final int pos = url.indexOf("/", 1);
		return url.substring(1, pos);
	}
}
