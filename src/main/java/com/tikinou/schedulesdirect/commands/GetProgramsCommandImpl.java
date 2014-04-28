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

package com.tikinou.schedulesdirect.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikinou.schedulesdirect.ClientUtils;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.BaseCommandResult;
import com.tikinou.schedulesdirect.core.commands.program.AbstractGetProgramsCommand;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommand;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommandResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.program.ProgramSD;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastien Astie
 */
public class GetProgramsCommandImpl extends AbstractGetProgramsCommand {
    private static Log LOG = LogFactory.getLog(GetProgramsCommand.class);
    @Override
    public void execute(SchedulesDirectClient client) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            String res = clientUtils.executeRequest(client,this, GetProgramsCommandResult.class, String.class);
            if(res == null)
                return;
            List<ProgramSD> list = new ArrayList<>();
            ObjectMapper mapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
            try (BufferedReader reader = new BufferedReader(new StringReader(res))){
                String line = reader.readLine();
                while(line != null){
                    ProgramSD val = mapper.readValue(line, ProgramSD.class);
                    if(val != null)
                        list.add(val);
                    line = reader.readLine();
                }
            }
            if(!list.isEmpty()) {
                GetProgramsCommandResult result = new GetProgramsCommandResult();
                result.setPrograms(list);
                setResults(result);
            }
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            GetProgramsCommandResult result = clientUtils.handleError(e, GetProgramsCommandResult.class, new GetProgramsCommandResult());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        if (getParameters().getProgramIds() == null || getParameters().getProgramIds().isEmpty())
            throw new ValidationException("programIds parameter is required");
    }
}
