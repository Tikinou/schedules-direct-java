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
import com.tikinou.schedulesdirect.core.commands.headend.AbstractGetHeadendsCommand;
import com.tikinou.schedulesdirect.core.commands.headend.GetHeadendsCommand;
import com.tikinou.schedulesdirect.core.commands.headend.GetHeadendsResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.Country;
import com.tikinou.schedulesdirect.core.domain.postalcode.DefaultPostalCodeFormatter;
import com.tikinou.schedulesdirect.core.domain.postalcode.PostalCodeFormatter;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sebastien Astie
 */
public class GetHeadendsCommandImpl extends AbstractGetHeadendsCommand {
    private static Log LOG = LogFactory.getLog(GetHeadendsCommand.class);
    private static PostalCodeFormatter POSTAL_CODE_FORMATTER = new DefaultPostalCodeFormatter();

    @Override
    public void execute(SchedulesDirectClient client) {
        ClientUtils clientUtils = ClientUtils.getInstance();
        try{
            clientUtils.failIfUnauthenticated(client.getCredentials());
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            clientUtils.executeRequest(client,this, GetHeadendsResult.class);
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            GetHeadendsResult result = clientUtils.handleError(e, GetHeadendsResult.class, new GetHeadendsResult());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        assert getParameters() != null;
        if(getParameters().getCountry() == null)
            throw new ValidationException("country parameter is required");
        if(getParameters().getPostalCode() == null)
            throw new ValidationException("postalCode parameter is required");
        getParameters().setPostalCode(POSTAL_CODE_FORMATTER.format(getParameters().getCountry(), getParameters().getPostalCode()));
    }
}
