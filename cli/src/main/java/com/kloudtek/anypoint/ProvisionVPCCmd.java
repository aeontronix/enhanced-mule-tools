package com.kloudtek.anypoint;

import com.kloudtek.anypoint.provisioning.VPCProvisioningDescriptor;
import com.kloudtek.util.FileUtils;
import com.kloudtek.util.UserDisplayableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;

@Command(name = "provisionvpc", description = "Provision VPC", sortOptions = false)
public class ProvisionVPCCmd extends AbstractOrganizationalCmd {
    private static final Logger logger = LoggerFactory.getLogger(ProvisionVPCCmd.class);
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    private boolean usageHelpRequested;
    @Option(names = {"-d", "--delete"}, description = "Delete pre-existing VPC with same name (and all applications in associated environments) if it exists prior to creation")
    private boolean delete;
    @Option(description = "JSON vpc descriptor file", names = {"-f", "--file"})
    private File file;

    @Override
    protected void execute(Organization organization) throws IOException, NotFoundException, HttpException {
        if( ! file.exists() ) {
            throw new UserDisplayableException("File doesn't exist: "+file.getPath());
        }
        AnypointClient client = organization.getClient();
        VPCProvisioningDescriptor vpcProvisioningDescriptor = client.getJsonHelper().getJsonMapper().readValue(file, VPCProvisioningDescriptor.class);
        if( delete ) {
            try {
                VPC preExistingVPC = organization.findVPCByName(vpcProvisioningDescriptor.getName());
                preExistingVPC.delete();
            } catch (NotFoundException e) {
                logger.debug("No pre-existing VPC exists");
            }
        }
        organization.provisionVPC(vpcProvisioningDescriptor);
    }
}
