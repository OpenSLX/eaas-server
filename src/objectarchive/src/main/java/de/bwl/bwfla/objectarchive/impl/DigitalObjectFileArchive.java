/*
 * This file is part of the Emulation-as-a-Service framework.
 *
 * The Emulation-as-a-Service framework is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * The Emulation-as-a-Service framework is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Emulation-as-a-Software framework.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package de.bwl.bwfla.objectarchive.impl;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;


import de.bwl.bwfla.common.datatypes.DigitalObjectMetadata;
import de.bwl.bwfla.common.taskmanager.TaskState;
import de.bwl.bwfla.common.utils.METS.MetsUtil;
import de.bwl.bwfla.objectarchive.datatypes.*;

import gov.loc.mets.Mets;
import org.apache.commons.io.FileUtils;
import org.apache.tamaya.inject.ConfigurationInjection;
import org.apache.tamaya.inject.api.Config;

import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.emucomp.api.Binding.ResourceType;
import de.bwl.bwfla.emucomp.api.Drive;
import de.bwl.bwfla.emucomp.api.EmulatorUtils;
import de.bwl.bwfla.emucomp.api.FileCollection;
import de.bwl.bwfla.emucomp.api.FileCollectionEntry;
import de.bwl.bwfla.objectarchive.utils.DefaultDriveMapper;


// FIXME: this class should be implemented in a style of a "Builder" pattern
public class DigitalObjectFileArchive implements Serializable, DigitalObjectArchive
{
	private static final long	serialVersionUID	= -3958997016973537612L;
	protected final Logger log	= Logger.getLogger(this.getClass().getName());

	private String name;
	private String localPath;
	private boolean defaultArchive;

	protected ObjectFileFilter objectFileFilter = new ObjectFileFilter();
	protected ObjectImportHandle importHandle;

	@Inject
	@Config(value="objectarchive.httpexport")
	public String httpExport;

	@Inject
	@Config(value="commonconf.serverdatadir")
	public String serverdatadir;

	private static final String METS_MD_FILENAME = "mets.xml";

	private String getExportPrefix()
	{
		String exportPrefix;
		try {
			exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8") + "/";
		} catch (UnsupportedEncodingException e) {
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		return exportPrefix;
	}

	/**
	 * Simple ObjectArchive example. Files are organized as follows
	 * localPath/
	 *          ID/
	 *            floppy/
	 *            iso/
	 *               disk1.iso
	 *               disk2.iso
	 *               
	 * Allowed extensions:
	 *      iso : {.iso}
	 *      floppy : {.img, .ima, .adf, .D64, .x64, .dsk, .st }
	 * 
	 * @param name
	 * @param localPath
	 */
	public DigitalObjectFileArchive(String name, String localPath, boolean defaultArchive)
	{
		this.init(name, localPath, defaultArchive);
	}

	protected DigitalObjectFileArchive() {}

	protected void init(String name, String localPath, boolean defaultArchive)
	{
		this.name = name;
		this.localPath = localPath;
		this.defaultArchive = defaultArchive;
		importHandle = new ObjectImportHandle(localPath);
		ConfigurationInjection.getConfigurationInjector().configure(this);
	}

	private static String strSaveFilename(String filename)
	{
		return filename.replace(" ", "");
	}

	private Path resolveMetadatTarget(String id) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		targetDir.resolve("metadata");
		if(!Files.exists(targetDir)) {
			try {
				Files.createDirectories(targetDir);
			} catch (IOException e) {
				throw new BWFLAException(e);
			}
		}
		return targetDir;
	}

	private Path resolveTarget(String id, ResourceType rt) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		targetDir = targetDir.resolve(rt.value());
		if(!targetDir.toFile().exists())
			if(!targetDir.toFile().mkdirs())
			{
				throw new BWFLAException("could not create directory: " + targetDir);
			}

		return targetDir;
	}

	private Path resolveTarget(String id, Drive.DriveType type) throws BWFLAException
	{
		if(type == null)
			return null;

		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		Path targetDir = objectDir.toPath().resolve(id);
		switch(type)
		{
			case CDROM:
				targetDir = targetDir.resolve("iso");
				break;
			case FLOPPY:
				targetDir = targetDir.resolve("floppy");
				break;
			case DISK:
				targetDir = targetDir.resolve("disk");
				break;
			case CART:
				targetDir = targetDir.resolve("cart");
				break;
			default:
				throw new BWFLAException("unsupported type " + type);
		}
		if(!targetDir.toFile().exists())
			if(!targetDir.toFile().mkdirs())
			{
				throw new BWFLAException("could not create directory: " + targetDir);
			}
		return targetDir;
	}

	public String getThumbnail(String id) throws BWFLAException {
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(id);
		Path target = targetDir.resolve("thumbnail.jpeg");
		if(!Files.exists(target))
			return null;

		String exportPrefix;
		try {
			exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8") + "/" + id;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		return exportPrefix + "/thumbnail.jpeg";
	}

	public void importObjectThumbnail(FileCollectionEntry resource) throws BWFLAException
	{
		File objectDir = new File(localPath);
		if(!objectDir.exists() && !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}
		Path targetDir = objectDir.toPath().resolve(resource.getId());
		if(!targetDir.toFile().exists()) {
			if (!targetDir.toFile().mkdirs()) {
				throw new BWFLAException("could not create directory: " + targetDir);
			}
		}

		Path target = targetDir.resolve("thumbnail.jpeg");
		if(Files.exists(target))
			return;
		EmulatorUtils.copyRemoteUrl(resource, target, null);
	}

	void importObjectFile(String objectId, FileCollectionEntry resource) throws BWFLAException
	{
		Path targetDir;

		if(resource.getType() != null)
			targetDir= resolveTarget(objectId, resource.getType());
		else if(resource.getResourceType() != null)
		{
			targetDir = resolveTarget(objectId, resource.getResourceType());
		}
		else throw new BWFLAException("could not determine drive/resource type of object");
		
		String fileName = resource.getLocalAlias();
		if(fileName == null || fileName.isEmpty())
			fileName = resource.getId();

		fileName = strSaveFilename(fileName);
		Path target = targetDir.resolve(fileName);

		EmulatorUtils.copyRemoteUrl(resource, target, null);
	}

	@Override
	public void importObject(String metsdata) throws BWFLAException {
		MetsObject o = new MetsObject(metsdata);
		if(o.getId() == null || o.getId().isEmpty())
			throw new BWFLAException("invalid object id " + o.getId());

		FileCollection fc = o.getFileCollection(null);

		if(fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		if(objectExits(o.getId()))
			return;

		for(FileCollectionEntry entry : fc.files)
			importObjectFile(o.getId(), entry);

		Mets m = fromFileCollection(o.getId(), fc);
		writeMetsFile(m);
	}

	@Override
	public Stream<String> getObjectIds()
	{
		final Path basepath = this.getLocalPath();
		if (!Files.exists(basepath)) {
			log.warning("No object-archive exists at " + basepath.toString() + "!");
			return Stream.empty();
		}

		try {
			final Function<Path, String> mapper = (path) -> {
				try {
					return new ObjectFileManifestation(objectFileFilter, path.toFile())
							.getId();
				}
				catch (BWFLAException error) {
					final String name = path.getFileName().toString();
					log.log(Level.WARNING, "Parsing object '" + name + "' failed!", error);
					return null;
				}
			};

			final DirectoryStream<Path> files = Files.newDirectoryStream(basepath);
			return StreamSupport.stream(files.spliterator(), false)
					.filter((path) -> Files.isDirectory(path))
					.map(mapper)
					.filter(Objects::nonNull)
					.onClose(() -> {
						try {
							files.close();
						}
						catch (Exception error) {
							log.log(Level.WARNING, "Closing directory-stream failed!", error);
						}
					});
		}
		catch (Exception exception) {
			log.log(Level.SEVERE, "Reading object-archive's directory failed!", exception);
			return Stream.empty();
		}
	}

	private boolean objectExits(String objectId)
	{
		if(objectId == null)
			return false;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return false;
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return false;
		}
		return true;
	}

	public void delete(String objectId) throws BWFLAException {
		if(objectId == null)
			throw new BWFLAException("object id was null");

		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + localPath + " does not exist");
		}

		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			throw new BWFLAException("objectDir " + objectDir + " does not exist");
		}

		try {
			FileUtils.deleteDirectory(objectDir);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BWFLAException(e);
		}
	}

	private Mets fromFileCollection(String objectId, FileCollection fc) throws BWFLAException {
		if(fc == null || fc.files == null)
			throw new BWFLAException("Invalid arguments");

		String label = fc.getLabel() != null ? fc.getLabel() : objectId;

		Mets m = MetsUtil.createMets(fc.id, label);
		for(FileCollectionEntry entry : fc.files) {
			MetsUtil.FileTypeProperties properties = new MetsUtil.FileTypeProperties();
			Path targetDir = null;
			if(entry.getType() != null)
			 	targetDir = resolveTarget(objectId, entry.getType());
			else if(entry.getResourceType() != null)
				targetDir = resolveTarget(objectId, entry.getResourceType());
			else
				throw new BWFLAException("invalid file entry type");

			String url = Paths.get(localPath).relativize(targetDir).toString();

			properties.fileFmt = entry.getResourceType() != null ? entry.getResourceType().toQID(): null;
			properties.deviceId = entry.getType() != null ? entry.getType().toQID() : null;

			String fileName = entry.getLocalAlias();
			if (fileName == null || fileName.isEmpty())
				fileName = entry.getId();
			url += "/" + fileName;

			log.warning(" local path url " + url);

			MetsUtil.addFile(m, url, properties);
		}
		return m;
	}

	private void createMetsFiles(String objectId) throws BWFLAException {
		FileCollection fc = getObjectReference(objectId);
		Mets m = fromFileCollection(objectId, fc);
		writeMetsFile(m);
	}

	private void writeMetsFile(Mets m) throws BWFLAException {
		Path targetDir = resolveMetadatTarget(m.getID());
		Path metsPath = targetDir.resolve(METS_MD_FILENAME);

		log.warning("local representation");
		log.warning(m.toString());

		try {
			Files.write( metsPath, m.toString().getBytes(), StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
			throw new BWFLAException(e);
		}
	}

	public FileCollection getObjectReference(String objectId)
	{
		if(objectId == null)
			return null;

		log.info("looking for: " + objectId);
		File topDir = new File(localPath);
		if(!topDir.exists() || !topDir.isDirectory())
		{
			log.warning("objectDir " + localPath + " does not exist");
			return null;
		}		
		
		File objectDir = new File(topDir, objectId);
		if(!objectDir.exists() || !objectDir.isDirectory())
		{
			log.warning("objectDir " + objectDir + " does not exist");
			return null;
		}

		ObjectFileManifestation mf = null;
		try {
			mf = new ObjectFileManifestation(objectFileFilter, objectDir);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		String exportPrefix;
		try {
			exportPrefix = httpExport + URLEncoder.encode(name, "UTF-8") + "/" + URLEncoder.encode(objectId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
		DefaultDriveMapper driveMapper = new DefaultDriveMapper(importHandle);
		try {
			return driveMapper.map(exportPrefix, mf);
		} catch (BWFLAException e) {
			// TODO Auto-generated catch block
			log.log(Level.WARNING, e.getMessage(), e);
			return null;
		}
		
	}

	public Path getLocalPath()
	{
		return Paths.get(localPath);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public static class ObjectImportHandle
	{
		private final File objectsDir;
		private final Logger log	= Logger.getLogger(this.getClass().getName());
		
		public ObjectImportHandle(String localPath)
		{
			this.objectsDir = new File(localPath);
		}
		
		public File getImportFile(String id, ResourceType rt)
		{
			Path targetDir = objectsDir.toPath().resolve(id);
			switch(rt)
			{
			case ISO: 
				// if(!fileName.endsWith("iso"))
				// 	fileName+=".iso";
				targetDir = targetDir.resolve("iso");
				if(!targetDir.toFile().exists())
					if(!targetDir.toFile().mkdirs())
					{
						log.warning("could not create directory: " + targetDir);
						return null;
					}
				return new File(targetDir.toFile(), "__import.iso");
			default:
				return null;
			}
		}
	}
	
	protected static class NullFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;

			if (file.getName().startsWith("."))
				return false;

			if (file.getName().endsWith(".properties"))
				return false;

			return true;
		}
	}
	
	protected static class IsoFileFilter implements FileFilter
	{
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check file's extension...
			final String name = file.getName();
			final int length = name.length();
			return (name.regionMatches(true, length - 4, ".iso", 0, 4)  || name.regionMatches(true, length - 4, ".bin", 0, 4));
		}
	};

	protected static class FloppyFileFilter implements FileFilter
	{
		private final Set<String> formats = new HashSet<String>();
		
		public FloppyFileFilter()
		{
			// Add all valid formats
			formats.add(".img");
			formats.add(".ima");
			formats.add(".adf");
			formats.add(".d64");
			formats.add(".x64");
			formats.add(".dsk");
			formats.add(".st");
			formats.add(".tap");
		}
		
		public boolean accept(File file)
		{
			if (file.isDirectory())
				return false;
			
			// Check the file's extension...
			final String name = file.getName();
			final int extpos = name.lastIndexOf('.');
			if (extpos < 0)
				return false;  // No file extension found!
			
			final String ext = name.substring(extpos);
			return formats.contains(ext.toLowerCase());
		}
	}

	@Override
	public DigitalObjectMetadata getMetadata(String objectId) throws BWFLAException {

		MetsObject o = loadMetsData(objectId);
		Mets m = MetsUtil.export(o.getMets(), getExportPrefix());
		DigitalObjectMetadata md = new DigitalObjectMetadata(m);

		String thumb = null;
		try {
			thumb = getThumbnail(objectId);
		} catch (BWFLAException e) {
			log.log(Level.WARNING, e.getMessage(), e);
		}
		if (thumb != null)
			md.setThumbnail(thumb);

		return md;
	}

	private MetsObject loadMetsData(String objectId) throws BWFLAException {
		Path targetDir = resolveMetadatTarget(objectId);
		Path metsPath = targetDir.resolve(METS_MD_FILENAME);
		if(!Files.exists(metsPath)) {
			createMetsFiles(objectId);
		}
		MetsObject mets = new MetsObject(metsPath.toFile());
		return mets;
	}

	@Override
	public Stream<DigitalObjectMetadata> getObjectMetadata() {

		final Function<String, DigitalObjectMetadata> mapper = (id) -> {
			try {
				return this.getMetadata(id);
			}
			catch (Exception error) {
				log.log(Level.WARNING, "Reading object's metadata failed!", error);
				return null;
			}
		};

		return this.getObjectIds()
				.map(mapper)
				.filter(Objects::nonNull);
	}

	@Override
	public void sync() {	
	}

	@Override
	public TaskState sync(List<String> objectId) {
		return null;
	}


	@Override
	public boolean isDefaultArchive() {
		return defaultArchive;
	}

	@Override
	public int getNumObjectSeats(String id) {
		return -1;
	}

	public static class ObjectFileFilter
	{
		public FileFilter ISO_FILE_FILTER = new NullFileFilter();
		public FileFilter FLOPPY_FILE_FILTER = new NullFileFilter();
	}
}
