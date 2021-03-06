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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikinou.schedulesdirect.core.ParameterizedCommand;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.headend.GetHeadendsCommand;
import com.tikinou.schedulesdirect.core.commands.headend.GetHeadendsParameters;
import com.tikinou.schedulesdirect.core.commands.image.GetImageCommand;
import com.tikinou.schedulesdirect.core.commands.image.GetImageParameters;
import com.tikinou.schedulesdirect.core.commands.lineup.*;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommand;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommandParameters;
import com.tikinou.schedulesdirect.core.commands.schedules.GetSchedulesCommand;
import com.tikinou.schedulesdirect.core.commands.schedules.GetSchedulesCommandParameters;
import com.tikinou.schedulesdirect.core.commands.status.GetStatusCommand;
import com.tikinou.schedulesdirect.core.commands.status.GetStatusCommandParameters;
import com.tikinou.schedulesdirect.core.domain.*;
import com.tikinou.schedulesdirect.core.exceptions.VersionNotSupportedException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImplTest {
    private static final int NUM_RETRIES = 2;
    private SchedulesDirectClient client;

    @Before
    public void setUp() throws Exception {
        ApplicationContext ctxt = new AnnotationConfigApplicationContext(SchedulesDirectConfig.class);
        client = ctxt.getBean(SchedulesDirectClient.class);
        client.setup(SchedulesDirectApiVersion.VERSION_20131021, null, true);
    }

    @Test
    public void testConnect() throws Exception {
        Credentials credentials = createCredentials();
        assert credentials.getToken() == null;
        client.connect(credentials);
        assert credentials.getToken() != null;
        System.out.println("TestConnect success: credentials now: " + credentials);
    }

    @Test
    public void testMultipleConnect() throws Exception {
        Credentials credentials = createCredentials();
        assert credentials.getToken() == null;
        client.connect(credentials);
        assert credentials.getToken() != null;
        client.connect(credentials);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void testUnknownVersion() throws Exception{
        client.setup(null, null, false);
    }


    @Test
    public void testStatus() throws Exception {
        Credentials credentials = connect();
        GetStatusCommand cmd = client.createCommand(GetStatusCommand.class);
        cmd.setParameters(new GetStatusCommandParameters());
        executeCommand(cmd, NUM_RETRIES);
    }

    @Test
    public void testLineups() throws Exception {
        Credentials credentials = connect();
        GetLineupDetailsCommand cmd = client.createCommand(GetLineupDetailsCommand.class);
        cmd.setParameters(new LineupCommandParameters("USA-NY67791-X"));
        executeCommand(cmd, NUM_RETRIES);
        GetLineupDetailsResult result = cmd.getResults();
        assert result != null;
    }

    @Test
    public void testPrograms() throws Exception {
        Credentials credentials = connect();
        GetProgramsCommand cmd = client.createCommand(GetProgramsCommand.class);
//        cmd.setParameters(new GetProgramsCommandParameters(Arrays.asList("EP017398160007", "SH013762600000", "MV003954050000")));
        cmd.setParameters(new GetProgramsCommandParameters(Arrays.asList("MV003954050000")));
        executeCommand(cmd, NUM_RETRIES);
    }

    @Test
    public void testImage() throws Exception {
        Credentials credentials = connect();
        GetImageCommand  cmd = client.createCommand(GetImageCommand.class);
        cmd.setParameters(new GetImageParameters("assets/p3561420_b_v5_aa.jpg"));
        executeCommand(cmd, NUM_RETRIES);

    }



    @Test
    public void testSchedules() throws Exception {
        Credentials credentials = connect();
        GetSchedulesCommand cmd = client.createCommand(GetSchedulesCommand.class);
        cmd.setParameters(new GetSchedulesCommandParameters(Arrays.asList("16689", "20360", "20453", "21868")));
        executeCommand(cmd, NUM_RETRIES);
    }

    @Test
    public void testGetSubscribedLineups() throws Exception {
        Credentials credentials = connect();
        GetSubscribedLineupsCommand cmd = client.createCommand(GetSubscribedLineupsCommand.class);
        cmd.setParameters(new GetSubscribedLineupsCommandParameters());
        executeCommand(cmd, NUM_RETRIES);
    }

    @Test
    public void testGetHeadends() throws Exception {
        Credentials credentials = connect();
        GetHeadendsCommand cmd = client.createCommand(GetHeadendsCommand.class);
        GetHeadendsParameters parameters =  new GetHeadendsParameters(null);
        parameters.setCountry(Country.UnitedStates);
        parameters.setPostalCode("10562");
        cmd.setParameters(parameters);
        executeCommand(cmd, NUM_RETRIES);
    }

    public void testAddAndDeleteLineups() throws Exception{
        Credentials credentials = connect();
        GetHeadendsCommand cmd = client.createCommand(GetHeadendsCommand.class);
        GetHeadendsParameters parameters =  new GetHeadendsParameters();
        parameters.setCountry(Country.UnitedStates);
        parameters.setPostalCode("94002");
        cmd.setParameters(parameters);
        executeCommand(cmd, NUM_RETRIES);
        System.out.println("Got Headends, try to find the first one and add it");
        assert !cmd.getResults().getHeadends().isEmpty();
        Headend headend = cmd.getResults().getHeadends().values().iterator().next();
        LineupManagementCommand mcmd = client.createCommand(AbstractAddLineupCommand.class);
        String uri = headend.getLineups().get(0).getUri();
        String id = uri.substring(uri.lastIndexOf("/") +1);
        LineupCommandParameters p = new LineupCommandParameters(id);
        mcmd.setParameters(p);
        System.out.println("Adding lineup " + p.getLineupId());
        executeCommand(mcmd, NUM_RETRIES);
        System.out.println("Added lineup " + p.getLineupId());
        mcmd = client.createCommand(AbstractDeleteLineupCommand.class);
        mcmd.setParameters(p);
        System.out.println("Deleting lineup " + p.getLineupId());
        executeCommand(mcmd, NUM_RETRIES);
        System.out.println("Deleted lineup " + p.getLineupId());
    }

    private Credentials connect() throws Exception {
        Credentials credentials = createCredentials();
        client.connect(credentials);
        return credentials;
    }

    private void executeCommand(ParameterizedCommand cmd, int numRetries) throws Exception{
        client.execute(cmd, numRetries);
        System.out.println(cmd.getResults());
        assert cmd.getStatus() == CommandStatus.SUCCESS;
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
