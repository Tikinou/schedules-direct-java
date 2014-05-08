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
import com.tikinou.schedulesdirect.core.commands.lineup.AbstractGetLineupDetailsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupDetailsResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Sebastien Astie
 */
public class GetLineupDetailsCommandImpl extends AbstractGetLineupDetailsCommand {
    private static Log LOG = LogFactory.getLog(GetLineupDetailsCommandImpl.class);

    @Override
    public String getEndPoint() {
        StringBuilder b = new StringBuilder(super.getEndPoint());
        b.append("/").append(getParameters().getLineupId());
        return b.toString();
    }

    @Override
    public void execute(SchedulesDirectClient client, int numRetries) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            while(numRetries >= 0) {
                try {
                    clientUtils.executeRequest(client, this, GetLineupDetailsResult.class);
                    break;
                } catch (HttpClientErrorException ex) {
                    numRetries = clientUtils.retryConnection(client, getParameters(), ex, numRetries);
                }
            }
        } catch (Exception e) {
            onError(clientUtils, e);
        }
    }


    private void onError(ClientUtils clientUtils, Exception e){
        LOG.error("Error while executing command.", e);
        setStatus(CommandStatus.FAILURE);
        GetLineupDetailsResult result = clientUtils.handleError(e, GetLineupDetailsResult.class, new GetLineupDetailsResult());
        setResults(result);
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        if(getParameters().getLineupId() == null || getParameters().getLineupId().isEmpty()){
            throw new ValidationException("lineupId parameter is required");
        }
    }
}
