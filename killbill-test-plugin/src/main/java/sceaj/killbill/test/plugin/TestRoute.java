/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package sceaj.killbill.test.plugin;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.Body;
import org.jooby.mvc.Consumes;
import org.jooby.mvc.Local;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;
import org.killbill.billing.tenant.api.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sceaj.killbill.test.plugin.model.TestData;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Random;


@Singleton
@Path("/tests/v1")
@Consumes("application/json")
@Produces("application/json")
public class TestRoute {

    private static final Logger log = LoggerFactory.getLogger(TestRoute.class);

    private Random random = new Random(System.currentTimeMillis());

    /**
     * Kill Bill automatically injects Tenant object in this method when this end point is accessed with the X-Killbill-ApiKey and X-Killbill-ApiSecret headers 
     * @param tenant
     */
    @POST
    public Result testEndpoint(@Local @Named("killbill_tenant") final Optional<Tenant> tenant,
                               @Body final TestData data) throws InterruptedException {
        // Find me on http://127.0.0.1:8080/plugins/killbill-test-plugin
        if(tenant != null && tenant.isPresent() ) {
        	log.info("tenant is available");
        	Tenant t1 = tenant.get();
        	log.info("tenant id:"+t1.getId());
        }
        else {
        	log.info("tenant is not available");
        }
        try {
            long delay = (long) random.nextInt( 100);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
        return Results.json(data).status(200);
    }
}
