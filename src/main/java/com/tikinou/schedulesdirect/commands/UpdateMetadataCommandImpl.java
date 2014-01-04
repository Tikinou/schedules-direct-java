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
import com.tikinou.schedulesdirect.core.CommandResult;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.BaseCommandResult;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupsCommand;
import com.tikinou.schedulesdirect.core.commands.metadata.AbstractUpdateMetadataCommand;
import com.tikinou.schedulesdirect.core.commands.metadata.UpdateMetadataCommand;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sebastien Astie
 */
public class UpdateMetadataCommandImpl extends AbstractUpdateMetadataCommand {
    private static Log LOG = LogFactory.getLog(UpdateMetadataCommand.class);
    @Override
    public void execute(SchedulesDirectClient client) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            ClientUtils.getInstance().executeRequest(client,this, BaseCommandResult.class);
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            BaseCommandResult result = new BaseCommandResult();
            result.setMessage(e.getMessage());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        if (getParameters().getSource() == null)
            throw new ValidationException("source parameter is required");
        if (getParameters().getComment() == null)
            throw new ValidationException("comment parameter is required");
        if (getParameters().getSuggested() == null)
            throw new ValidationException("suggested parameter is required");
        if (getParameters().getCurrent() == null)
            throw new ValidationException("current series id parameter is required");
        if (getParameters().getProgramId() == null)
            throw new ValidationException("programId parameter is required");
        if (getParameters().getField() == null)
            throw new ValidationException("field parameter is required");
    }
}
