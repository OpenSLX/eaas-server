package de.bwl.bwfla.emil.tasks;

import de.bwl.bwfla.api.imagearchive.*;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import de.bwl.bwfla.common.taskmanager.AbstractTask;
import de.bwl.bwfla.emil.DatabaseEnvironmentsAdapter;
import de.bwl.bwfla.emil.EmilEnvironmentRepository;
import de.bwl.bwfla.emucomp.api.ImageArchiveBinding;
import de.bwl.bwfla.emucomp.api.MachineConfiguration;
import de.bwl.bwfla.emucomp.api.MachineConfigurationTemplate;
import de.bwl.bwfla.imagearchive.util.EnvironmentsAdapter;
import de.bwl.bwfla.imageproposer.client.ImageProposer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImportImageTask extends AbstractTask<Object> {
    private ImportImageTaskRequest request;
    private Logger log;

    public ImportImageTask(ImportImageTaskRequest request, Logger log)
    {
       this.request = request;
       this.log = log;
    }

    public static class ImportImageTaskRequest
    {
        public String label;
        public URL url;

        public DatabaseEnvironmentsAdapter environmentHelper;

        public String destArchive;

        public void validate() throws BWFLAException
        {
            if(url == null || destArchive == null)
                throw new BWFLAException("ImportImageTaskRequest: input validation failed");

            if(environmentHelper == null )
                throw new BWFLAException("ImportImageTaskRequest: missing dependencies");
        }
    }

    @Override
    protected Object execute() throws Exception {
        try {
            ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
            iaMd.setType(ImageType.USER);

            TaskState importState = request.environmentHelper.importImage(request.url, iaMd, true);
            while(!importState.isDone())
            {
                importState = request.environmentHelper.getState(importState.getTaskId());
            }

            Map<String, String> userData = new HashMap<>();
            String imageId = importState.getResult();
            userData.put("imageId", imageId);

            ImageMetadata entry = new ImageMetadata();
            entry.setName(request.label);
            ImageDescription description = new ImageDescription();
            description.setId(imageId);
            entry.setImage(description);

            request.environmentHelper.addNameIndexesEntry(request.destArchive, entry,null);

            return userData;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return e;
        }
    }
}

// old import code.. to be refactored

/*

    @Override
    protected Object execute() throws Exception {
        try {
            MachineConfigurationTemplate pEnv = request.environmentHelper.getTemplate(request.templateId);
            if (pEnv == null)
                return new BWFLAException("invalid template id: " + request.templateId);
            MachineConfiguration env = pEnv.implement();

            ImageArchiveMetadata iaMd = new ImageArchiveMetadata();
            iaMd.setType(ImageType.TMP);

            EnvironmentsAdapter.ImportImageHandle importState = null;
            importState = request.environmentHelper.importImage(request.destArchive, request.url, iaMd, true);

            ImageArchiveBinding binding = importState.getBinding(60 * 60 * 60); // wait an hour
            if (binding == null)
                return new BWFLAException("ImportImageTask: import image failed. Could not create binding");
            if (request.patchId != null) {
                binding = request.environmentHelper.generalizedImport(request.destArchive, binding.getImageId(),
                        iaMd.getType(), request.patchId);
            }
            binding.setId("main_hdd");
            env.getAbstractDataResource().add(binding);

            env.getDescription().setTitle(request.label);
            if (env.getNativeConfig() == null)
                env.setNativeConfig(new MachineConfiguration.NativeConfig());
            env.getNativeConfig().setValue(request.nativeConfig);

            if (request.romFile != null) {
                iaMd.setType(ImageType.ROMS);
                DataHandler handler = new DataHandler(new FileDataSource(request.romFile));

                importState = request.environmentHelper.importImage(request.destArchive, handler, iaMd);
                ImageArchiveBinding romBinding = importState.getBinding(60 * 60 * 60); // wait an hour
                romBinding.setId("rom-" + request.romFile.getName());
                env.getAbstractDataResource().add(romBinding);

            }
            String newEnvironmentId = request.environmentHelper.importMetadata(request.destArchive, env, iaMd, false);
            Map<String, String> userData = new HashMap<>();
            userData.put("environmentId", newEnvironmentId);
            request.imageProposer.refreshIndex();
            return userData;
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            return e;
        }
    }*/
