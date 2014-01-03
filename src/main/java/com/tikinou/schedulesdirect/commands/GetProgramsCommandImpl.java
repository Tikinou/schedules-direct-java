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

import com.tikinou.schedulesdirect.ClientUtils;
import com.tikinou.schedulesdirect.core.FileUrlBasedCommandResult;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.BaseFileUrlBasedCommandResult;
import com.tikinou.schedulesdirect.core.commands.lineup.AbstractGetLineupsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupsCommand;
import com.tikinou.schedulesdirect.core.commands.program.AbstractGetProgramsCommand;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommand;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            ClientUtils.getInstance().executeRequest(client,this, getParameters(), BaseFileUrlBasedCommandResult.class);
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            FileUrlBasedCommandResult result = new BaseFileUrlBasedCommandResult();
            result.setMessage(e.getMessage());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        if (getParameters().getProgramIds() == null || getParameters().getProgramIds().isEmpty())
            throw new ValidationException("programIds parameter is required");
    }
}