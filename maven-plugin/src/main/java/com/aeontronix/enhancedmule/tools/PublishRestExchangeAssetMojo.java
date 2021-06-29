/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.commons.validation.ValidationUtils;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningRequest;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningRequestImpl;
import com.aeontronix.enhancedmule.tools.exchange.APISpecSource;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.IconDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.InvalidAnypointDescriptorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Set;

@Mojo(name = "publish-rest-asset", defaultPhase = LifecyclePhase.DEPLOY)
public class PublishRestExchangeAssetMojo extends AbstractOrganizationalMojo {
    @Parameter(property = "emt.apiSpecDir", defaultValue = "src${file.separator}main${file.separator}api")
    private File apiSpecDir;
    @Parameter(property = "emt.assetDescriptorFile", defaultValue = "exchange-asset.json")
    private File assetDescriptorFile;
    @Parameter(property = "emt.buildNumber")
    private String buildNumber;
    @Parameter(property = "emt.asset.pages", defaultValue = "${project.basedir}${file.separator}src${file.separator}main${file.separator}pages")
    private File assetPagesDir;
    @Parameter(property = "publishrestasset.skip", defaultValue = "false")
    private boolean skip;
    @Parameter(property = "emt.provisioning.deletesnapshots", defaultValue = "true")
    private boolean deleteSnapshots;

    public PublishRestExchangeAssetMojo() {
    }

    @Override
    protected void doExecute() throws Exception {
        if( skip ) {
            return;
        }
        if( ! apiSpecDir.exists() ) {
            throw new IOException(apiSpecDir+" doesn't exist");
        } else if( ! apiSpecDir.isDirectory() ) {
            throw new IOException(apiSpecDir+" isn't a directory");
        }
        final ObjectMapper om = new ObjectMapper();
        final ExchangeAssetDescriptor asset = om.readValue(assetDescriptorFile, ExchangeAssetDescriptor.class);
        if(asset.getId() == null) {
            asset.setId(project.getArtifactId());
        }
        if( asset.getGroupId() == null ) {
            asset.setGroupId(getOrganization().getId());
        }
        if(asset.getVersion() == null) {
            asset.setVersion(project.getVersion());
        }
        if( asset.getVersion().toLowerCase().endsWith("-snapshot") ) {
            if( buildNumber == null ) {
                buildNumber = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS").format(LocalDateTime.now());
            }
            asset.setVersion(asset.getVersion()+ "-" + buildNumber);
        }
        if(asset.getName() == null) {
            asset.setName(project.getName());
        }
        if(asset.getDescription() == null) {
            asset.setDescription(project.getDescription());
        }
        if( asset.getAssetMainFile() == null ) {
            asset.setAssetMainFile(ApplicationDescriptorProcessorImpl.findAPISpecFile(asset.getId(),apiSpecDir));
        }
        if( asset.getApiVersion() == null ) {
            final String majorVersion = asset.getMajorVersion();
            if( "raml".equalsIgnoreCase(asset.getClassifier()) ) {
                asset.setApiVersion("v"+majorVersion);
            } else {
                asset.setApiVersion(majorVersion+".0.0");
            }
        }
        if( asset.getIcon() == null ) {
            final File iconFile = ExchangeAssetDescriptor.findIcon(project.getBasedir());
            if( iconFile != null ) {
                asset.setIcon(new IconDescriptor(iconFile.getPath()));
            }
        }
        asset.setCreate(true);
        asset.findPages(assetPagesDir);
        ValidationUtils.validate(asset, InvalidAnypointDescriptorException.class);
        final HashMap<String, File> specFiles = new HashMap<>();
        addFile(specFiles,null,apiSpecDir);
        final ProvisioningRequest provisioningRequest = new ProvisioningRequestImpl(buildNumber,deleteSnapshots,true);
        asset.publish(getOrganization(), new APISpecSource() {
            @Override
            public Set<String> listAPISpecFiles() throws IOException {
                return specFiles.keySet();
            }

            @Override
            public void writeAPISpecFile(String name, OutputStream os) throws IOException {
                try( final FileInputStream fis = new FileInputStream(specFiles.get(name)) ) {
                    IOUtils.copy(fis,os);
                }
            }
        }, provisioningRequest);
        asset.provision(getOrganization());
    }

    private void addFile(HashMap<String, File> specFiles, String path, File file) {
        if( file.isDirectory() ) {
            for (File f : file.listFiles()) {
                String p = f.getName();
                if( path != null ) {
                    p = path + "/" + p;
                }
                addFile(specFiles,p,f);
            }
        } else {
            specFiles.put(path,file);
        }
    }
}
