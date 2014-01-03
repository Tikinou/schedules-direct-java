/*
 * Copyright (c) 2014 TIKINOU LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tikinou.schedulesdirect;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.domain.SchedulesDirectApiVersion;
import com.tikinou.schedulesdirect.core.exceptions.VersionNotSupportedException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImplTest {
    private SchedulesDirectClient client;

    @Before
    public void setUp() throws Exception {
        ApplicationContext ctxt = new AnnotationConfigApplicationContext(SchedulesDirectConfig.class);
        client = ctxt.getBean(SchedulesDirectClient.class);
        client.setup(SchedulesDirectApiVersion.VERSION_20131021, true);
    }

    @Test
    public void testConnect() throws Exception {
        Credentials credentials = createCredentials();
        client.connect(credentials, true);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void testUnknownVersion() throws Exception{
        client.setup(null, false);
    }


    private Credentials createCredentials() throws IOException {
        ObjectMapper mapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
        Credentials credentials = mapper.readValue(SchedulesDirectClientImplTest.class.getResourceAsStream("/credentials.json"), Credentials.class);
        credentials.setClearPassword(credentials.getPassword());
        // override from system props (can be provided from gradle.properties)
        String userName = System.getProperty("credentials.username");
        if (userName != null)
            credentials.setUsername(userName);
        String password = System.getProperty("credentials.password");
        if (password != null)
            credentials.setClearPassword(password);

        assert credentials.getUsername() != null && !"CHANGE_USER_NAME".equals(credentials.getUsername());
        assert credentials.getClearPassword() != null &&  !"CHANGE_PASSWORD".equals(credentials.getClearPassword());
        return credentials;
    }
}
