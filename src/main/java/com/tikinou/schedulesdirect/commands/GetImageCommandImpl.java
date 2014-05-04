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
import com.tikinou.schedulesdirect.core.commands.image.AbstractGetImageCommand;
import com.tikinou.schedulesdirect.core.commands.image.GetImageResult;
import com.tikinou.schedulesdirect.core.commands.status.GetStatusCommand;
import com.tikinou.schedulesdirect.core.commands.status.GetStatusResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sebastien Astie.
 */
public class GetImageCommandImpl extends AbstractGetImageCommand {
    private static Log LOG = LogFactory.getLog(GetStatusCommand.class);

    @Override
    public void execute(SchedulesDirectClient client) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            byte[] res = clientUtils.executeRequest(client,this, GetImageResult.class, byte[].class);
            if(res == null)
                return;
            GetImageResult r = new GetImageResult();
            r.setImage(res);
            setResults(r);
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            GetImageResult result = clientUtils.handleError(e, GetImageResult.class, new GetImageResult());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
    }
}
