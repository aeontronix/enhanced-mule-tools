/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.cloudhub.CHMuleVersion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHRegion;
import com.aeontronix.enhancedmule.tools.cloudhub.CHWorkerType;
import com.aeontronix.enhancedmule.tools.cloudhub.MuleVersionUpdate;
import com.aeontronix.enhancedmule.tools.fabric.Fabric;
import com.aeontronix.enhancedmule.tools.runtime.Target;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeploymentTestsHelper {
    public static final String FILE_JAR = "file.jar";
    public static final String APP_NAME = "myapp";
    public static final String ARTIFACT_ID = "appartid";
    public static final String BUILD_NUMBER = "1000";
    public static final String ORG_ID = "428536327643427636";
    public static final String ORG_NAME = "MyOrg";
    public static final String MICRO = "micro";
    public static final String US_WEST_4 = "us-west-4";
    public static final String MYFABRIC = "myfabric";
    public static final String ENV_ID = "42798472398234-243432243-243432234";
    public static final String ENV_NAME = "ProdEnv";

    public static Environment createMockEnvironment(AnypointClient anypointClient) throws HttpException, NotFoundException {
        final Organization mockOrganization = createMockOrganization();
        Environment environment = mock(Environment.class);
        when(environment.getName()).thenReturn(ENV_NAME);
        when(environment.getId()).thenReturn(ENV_ID);
        when(environment.getType()).thenReturn(Environment.Type.PRODUCTION);
        when(environment.refresh()).thenReturn(environment);
        lenient().when(environment.findCHMuleVersion("1.2.3")).thenReturn(new CHMuleVersion("1.2.3", new MuleVersionUpdate("5323")));
        when(environment.findDefaultCHMuleVersion()).thenReturn(new CHMuleVersion("4.3.55", new MuleVersionUpdate("3434")));
        when(environment.findDefaultCHRegion()).thenReturn(new CHRegion(DeploymentTestsHelper.US_WEST_4,"zoomgalan"));
        when(environment.getClient()).thenReturn(anypointClient);
        lenient().when(environment.findWorkerTypeByName("huge")).thenReturn(new CHWorkerType("huge"));
        lenient().when(environment.findWorkerTypeByName("gigantic")).thenReturn(new CHWorkerType("gigantic"));
        when(environment.findSmallestWorkerType()).thenReturn(new CHWorkerType(DeploymentTestsHelper.MICRO));
        when(environment.getSuffix()).thenReturn("-prod");
        when(environment.getOrganization()).thenReturn(mockOrganization);
        lenient().when(environment.findServerByName(DeploymentTestsHelper.MYFABRIC)).thenThrow(new NotFoundException());
        return environment;
    }

    public static Organization createMockOrganization() throws NotFoundException, HttpException {
        Organization organization = mock(Organization.class);
        when(organization.getId()).thenReturn(DeploymentTestsHelper.ORG_ID);
        when(organization.getName()).thenReturn(DeploymentTestsHelper.ORG_NAME);
        lenient().when(organization.findFabricByName(DeploymentTestsHelper.MYFABRIC)).thenReturn(new Fabric());
        final Target fabricTarget = new Target();
        fabricTarget.setRuntimes(singletonList(new Target.Runtime("mule", singletonList(new Target.RuntimeVersion("1.2.3")))));
        lenient().when(organization.findTargetById(any())).thenReturn(fabricTarget);
        return organization;
    }

    public static AnypointClient createAnypointClient() throws HttpException {
        final AnypointClient anypointClient = mock(AnypointClient.class);
        HttpHelper httpHelper = mock(HttpHelper.class);
        lenient().when(httpHelper.anypointHttpPost(any(),any(),any())).thenReturn("{}");
        final JsonHelper jsonHelper = new JsonHelper(anypointClient);
        when(anypointClient.getJsonHelper()).thenReturn(jsonHelper);
        when(anypointClient.getHttpHelper()).thenReturn(httpHelper);
        return anypointClient;
    }
}
