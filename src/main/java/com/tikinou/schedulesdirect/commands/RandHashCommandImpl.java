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
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.randhash.AbstractRandhashCommand;
import com.tikinou.schedulesdirect.core.commands.randhash.RandHashCommand;
import com.tikinou.schedulesdirect.core.commands.randhash.RandHashParameters;
import com.tikinou.schedulesdirect.core.commands.randhash.RandHashResult;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.exceptions.ValidationException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * @author Sebastien Astie
 */
public class RandHashCommandImpl extends AbstractRandhashCommand {
    private static Log LOG = LogFactory.getLog(RandHashCommand.class);
    /**
     * @return the requestEntity
     */
    protected HttpEntity<?> getRequestEntity(Object body, MediaType...mediaTypes ) {

        if( null == mediaTypes || mediaTypes.length == 0 ) {
            mediaTypes = new MediaType[ 1 ];
            mediaTypes[ 0 ] = MediaType.APPLICATION_JSON;
        }

        if( mediaTypes.length > 1 ) {
            throw new IllegalArgumentException( "Should only be one MediaType here" );
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept( Arrays.asList(mediaTypes) );
        requestHeaders.add( "Connection", "Close" );
        requestHeaders.add( "User-Agent", "tikinou-sd-api" );
        return new HttpEntity<>(body,  requestHeaders );
    }

    @Override
    public void execute(SchedulesDirectClient client) {
        try{
            setStatus(CommandStatus.RUNNING);
            validateParameters();
            RestTemplate rt = RestTemplateFactory.getRestTemplate();
            MultiValueMap<String,RandHashParameters> valueMap = new LinkedMultiValueMap<>();
            valueMap.add("request", getParameters());
            HttpEntity<?> entity = getRequestEntity(valueMap);
            ResponseEntity<RandHashResult> res = rt.postForEntity(client.getUrl(), entity, RandHashResult.class);
            if(res.getStatusCode() == HttpStatus.OK){
                RandHashResult result = res.getBody();
                setResults(result);
                Credentials credentials = getParameters().getCredentials();
                credentials.setRandhash(result.getRandhash());
                credentials.setRandhashDateTime(DateTime.now());
                setStatus(CommandStatus.SUCCESS);
            }
        } catch (Exception e){
            LOG.error("Error while executing command.", e);
            setStatus(CommandStatus.FAILURE);
            RandHashResult result = new RandHashResult();
            result.setMessage(e.getMessage());
            setResults(result);
        }
    }

    @Override
    public void validateParameters() throws ValidationException {
        RandHashParameters parameters = getParameters();
        if(parameters.getCredentials().getUsername() == null || parameters.getCredentials().getUsername().isEmpty())
            throw new ValidationException("username must be provided");
    }
}
