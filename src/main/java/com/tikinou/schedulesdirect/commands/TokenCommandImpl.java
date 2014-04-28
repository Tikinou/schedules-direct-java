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
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.token.AbstractTokenCommand;
import com.tikinou.schedulesdirect.core.commands.token.TokenCommand;
import com.tikinou.schedulesdirect.core.commands.token.TokenParameters;
import com.tikinou.schedulesdirect.core.commands.token.TokenResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

/**
 * @author Sebastien Astie
 */
public class TokenCommandImpl extends AbstractTokenCommand {
    private static Log LOG = LogFactory.getLog(TokenCommand.class);


    @Override
    public void execute(SchedulesDirectClient client) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            clientUtils.executeRequest(client,this, TokenResult.class);
           if(getStatus() == CommandStatus.SUCCESS){
               Credentials credentials = getParameters().getCredentials();
               credentials.setToken(getResults().getToken());
               credentials.setTokenDateTime(DateTime.now());
               return;
           }
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            TokenResult result = clientUtils.handleError(e, TokenResult.class, new TokenResult());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        TokenParameters parameters = getParameters();
        if(parameters.getCredentials().getUsername() == null || parameters.getCredentials().getUsername().isEmpty())
            throw new ValidationException("username must be provided");
    }
}
