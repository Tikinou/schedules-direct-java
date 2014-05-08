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
import com.tikinou.schedulesdirect.core.commands.schedules.AbstractGetSchedulesCommand;
import com.tikinou.schedulesdirect.core.commands.schedules.GetSchedulesCommand;
import com.tikinou.schedulesdirect.core.commands.schedules.GetSchedulesCommandResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.schedule.ScheduleSD;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastien Astie
 */
public class GetSchedulesCommandImpl extends AbstractGetSchedulesCommand {
    private static Log LOG = LogFactory.getLog(GetSchedulesCommand.class);
    @Override
    public void execute(SchedulesDirectClient client, int numRetries) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            while(numRetries >= 0) {
                try {
                    coreExecute(client, clientUtils);
                    break;
                } catch (HttpClientErrorException ex) {
                    numRetries = clientUtils.retryConnection(client, getParameters(), ex, numRetries);
                }
            }
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            GetSchedulesCommandResult result = clientUtils.handleError(e, GetSchedulesCommandResult.class, new GetSchedulesCommandResult());
            setResults(result);
        }
    }

    private void coreExecute(SchedulesDirectClient client, ClientUtils clientUtils) throws Exception {
        String res = clientUtils.executeRequest(client,this, GetSchedulesCommandResult.class, String.class);
        if(res == null)
            return;
        List<ScheduleSD> list = new ArrayList<>();
        ObjectMapper mapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
        try (BufferedReader reader = new BufferedReader(new StringReader(res))){
            String line = reader.readLine();
            while(line != null){
                ScheduleSD val = mapper.readValue(line, ScheduleSD.class);
                if(val != null)
                    list.add(val);
                line = reader.readLine();
            }
        }
        if(!list.isEmpty()) {
            GetSchedulesCommandResult result = new GetSchedulesCommandResult();
            result.setSchedules(list);
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        if (getParameters().getStationIds() == null || getParameters().getStationIds().isEmpty())
            throw new ValidationException("stationIds parameter is required");
    }
}
