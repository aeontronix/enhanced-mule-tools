/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.util.AbstractAnypointTest;

public class APIProvisioningITCase extends AbstractAnypointTest {
    //    @Test
    public void testProvisioning() throws Exception {
//        integrationTest = true;
//        createAPIAsset(TESTAPI1, true);
//        createAPIAsset(TESTAPI2, true);
//        APIProvisioningConfig config = new APIProvisioningConfig();
//        config.setVariable("url", "http://foo");
//        // provision api 1
//        AnypointDescriptor apd1 = addClientIdPolicy(new AnypointDescriptor(TESTAPI1, V1));
//        ClientApplicationDescriptor clientApp1 = new ClientApplicationDescriptor();
////        apd1.setClientApp(clientApp1);
//        APIProvisioningResult res1 = provision(config, apd1);
//        // provision api 2
//        AnypointDescriptor apd2 = addClientIdPolicy(new AnypointDescriptor(TESTAPI2, V1));
//        APIDescriptor apd2api = apd2.getApi();
//        ClientApplicationDescriptor clientApp2 = new ClientApplicationDescriptor();
//        apd2api.setClientApp(clientApp2);
//        clientApp2.addAccess(res1.getApi());
//        apd2api.addSlaTier(new SLATierDescriptor("testtier", false, new SLATierLimits(true, 1, 1)));
//        APIProvisioningResult res2 = provision(config, apd2);
//        // test changing client id expression
//        assertEquals(2, env.findAPIs(null).size());
//        checkPolicy(TESTAPI2, V1, ATTRIBUTES_HEADERS_CLIENT_SECRET);
////        apd2api.getPolicies().get(0).getConfigurationData().put(CLIENT_SECRET_EXPRESSION, ATTRIBUTES_HEADERS_CLIENT_SECRET2);
//        provision(config, apd2);
//        checkPolicy(TESTAPI2, V1, ATTRIBUTES_HEADERS_CLIENT_SECRET2);
//        // request access to api2 from api1
//        apd1.getApi().addAccess(res2.getApi());
//        provision(config, apd1);
//        res2.getApi().refresh();
    }
}
