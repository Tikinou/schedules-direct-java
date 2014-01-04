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
import com.tikinou.schedulesdirect.core.Command;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.headend.*;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupsCommandParameters;
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
import java.util.ArrayList;
import java.util.Arrays;

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
        assert credentials.getRandhash() == null;
        client.connect(credentials);
        assert credentials.getRandhash() != null;
        System.out.println("TestConnect success: credentials now: " + credentials);
    }

    @Test
    public void testMultipleConnect() throws Exception {
        Credentials credentials = createCredentials();
        assert credentials.getRandhash() == null;
        client.connect(credentials);
        assert credentials.getRandhash() != null;
        client.connect(credentials);
    }

    @Test(expected = VersionNotSupportedException.class)
    public void testUnknownVersion() throws Exception{
        client.setup(null, false);
    }


    @Test
    public void testStatus() throws Exception {
        Credentials credentials = connect();
        GetStatusCommand cmd = client.createCommand(GetStatusCommand.class);
        cmd.setParameters(new GetStatusCommandParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021));
        executeCommand(cmd);
    }

    @Test
    public void testLineups() throws Exception {
        Credentials credentials = connect();
        GetLineupsCommand cmd = client.createCommand(GetLineupsCommand.class);
        GetLineupsCommandParameters parameters =  new GetLineupsCommandParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setHeadendIds(Arrays.asList("NY67791"));
        cmd.setParameters(parameters);
        executeCommand(cmd);
    }

    @Test
    public void testPrograms() throws Exception {
        Credentials credentials = connect();
        GetProgramsCommand cmd = client.createCommand(GetProgramsCommand.class);
        GetProgramsCommandParameters parameters =  new GetProgramsCommandParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setProgramIds(Arrays.asList("EP017398160007", "SH013762600000", "MV003954050000"));
        cmd.setParameters(parameters);
        executeCommand(cmd);
    }

    @Test
    public void testSchedules() throws Exception {
        Credentials credentials = connect();
        GetSchedulesCommand cmd = client.createCommand(GetSchedulesCommand.class);
        GetSchedulesCommandParameters parameters =  new GetSchedulesCommandParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setStationIds(Arrays.asList("16689", "20360", "20453", "21868"));
        cmd.setParameters(parameters);
        executeCommand(cmd);
    }

    @Test
    public void testGetSubscribedHeadends() throws Exception {
        Credentials credentials = connect();
        GetHeadendsCommand cmd = client.createCommand(GetHeadendsCommand.class);
        GetHeadendsParameters parameters =  new GetHeadendsParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setSubscribed(true);
        cmd.setParameters(parameters);
        executeCommand(cmd);
    }

    @Test
    public void testGetHeadends() throws Exception {
        Credentials credentials = connect();
        GetHeadendsCommand cmd = client.createCommand(GetHeadendsCommand.class);
        GetHeadendsParameters parameters =  new GetHeadendsParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setCountry(Country.UnitedStates);
        parameters.setPostalCode("10564");
        cmd.setParameters(parameters);
        executeCommand(cmd);
    }

    public void testAddAndDeleteHeadends() throws Exception{
        Credentials credentials = connect();
        GetHeadendsCommand cmd = client.createCommand(GetHeadendsCommand.class);
        GetHeadendsParameters parameters =  new GetHeadendsParameters(credentials.getRandhash(), SchedulesDirectApiVersion.VERSION_20131021);
        parameters.setCountry(Country.UnitedStates);
        parameters.setPostalCode("94002");
        cmd.setParameters(parameters);
        executeCommand(cmd);
        System.out.println("Got Headends, try to find the first one and add it");
        assert !cmd.getResults().getData().isEmpty();
        Headend headend = cmd.getResults().getData().get(0);
        AddHeadendCommand addCmd = client.createCommand(AddHeadendCommand.class);
        AddDeleteHeadendParameters addParams = new AddDeleteHeadendParameters(credentials.getRandhash(), false, SchedulesDirectApiVersion.VERSION_20131021);
        addParams.setHeadendId(headend.getHeadend());
        addCmd.setParameters(addParams);
        System.out.println("Adding headend " + headend.getHeadend());
        executeCommand(addCmd);
        System.out.println("Added headend " + headend.getHeadend());
        DeleteHeadendCommand delCmd = client.createCommand(DeleteHeadendCommand.class);
        AddDeleteHeadendParameters delParams = new AddDeleteHeadendParameters(credentials.getRandhash(), true, SchedulesDirectApiVersion.VERSION_20131021);
        delParams.setHeadendId(headend.getHeadend());
        delCmd.setParameters(delParams);
        System.out.println("Deleting headend " + headend.getHeadend());
        executeCommand(delCmd);
        System.out.println("Deleted headend " + headend.getHeadend());
    }

    private Credentials connect() throws Exception {
        Credentials credentials = createCredentials();
        client.connect(credentials);
        return credentials;
    }

    private void executeCommand(Command cmd) throws Exception{
        client.execute(cmd);
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
