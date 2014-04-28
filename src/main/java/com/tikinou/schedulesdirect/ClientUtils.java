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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tikinou.schedulesdirect.core.CommandResult;
import com.tikinou.schedulesdirect.core.ParameterizedCommand;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.AuthenticatedBaseCommandParameter;
import com.tikinou.schedulesdirect.core.commands.BaseCommandParameter;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.exceptions.AuthenticationException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Sebastien Astie.
 */
public class ClientUtils {
    private static final ClientUtils INSTANCE = new ClientUtils();
    public static ClientUtils getInstance(){
        return INSTANCE;
    }

    private RestTemplate restTemplate;

    private ClientUtils(){
        restTemplate = new RestTemplate();
//        restTemplate.setErrorHandler(new ErrorHandler());
        prepareMessageConverters(restTemplate.getMessageConverters());
    }

    private void prepareMessageConverters(List<HttpMessageConverter<?>> converters){
        for( HttpMessageConverter<?> messageConverter : converters ) {
            if( messageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
                MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) messageConverter;
                mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN, MediaType.APPLICATION_OCTET_STREAM));
                mappingJackson2HttpMessageConverter.setObjectMapper( objectMapper );
            }
        }
    }

    public <R_IMPL extends R, P extends BaseCommandParameter, R extends CommandResult, C extends ParameterizedCommand<P,R>> void executeRequest(SchedulesDirectClient client, C command, Class<R_IMPL> resultType){
        executeRequest(client, command, resultType, null);
    }

    public <R_IMPL extends R, P extends BaseCommandParameter, R extends CommandResult, C extends ParameterizedCommand<P,R>, R_OVER> R_OVER executeRequest(SchedulesDirectClient client, C command, Class<R_IMPL> resultType, Class<R_OVER> resulTypetOverride){
        StringBuilder url = new StringBuilder(client.getUrl());
        url.append("/").append(command.getEndPoint());
        String token = null;
        if(command.getParameters() instanceof AuthenticatedBaseCommandParameter)
            token =  ((AuthenticatedBaseCommandParameter)command.getParameters()).getToken();

        org.springframework.http.HttpMethod httpMethod = null;
        switch (command.getMethod()){
            case GET:
                Map<String, String> reqParams = command.getParameters().toRequestParameters();
                if(reqParams != null){
                    url.append("?");
                    int i = 0;
                    for(Map.Entry<String, String> entry : reqParams.entrySet()){
                        if(i > 0)
                            url.append("&");
                        url.append(entry.getKey()).append("=").append(entry.getValue());
                        i++;
                    }
                }
                httpMethod = org.springframework.http.HttpMethod.GET;
                break;
            case POST: {
                httpMethod = org.springframework.http.HttpMethod.POST;
                break;
            }
            case PUT: {
                httpMethod = org.springframework.http.HttpMethod.PUT;
                break;
            }
            case DELETE: {
                httpMethod = org.springframework.http.HttpMethod.DELETE;
                break;
            }
        }

        if(resulTypetOverride == null){
            ResponseEntity<R_IMPL> res = restTemplate.exchange(url.toString(), httpMethod, getRequestEntity(command.getParameters(), token), resultType);
            if(res.getStatusCode() == HttpStatus.OK){
                R result = res.getBody();
                command.setResults(result);
                command.setStatus(CommandStatus.SUCCESS);
            } else {
                command.setStatus(CommandStatus.FAILURE);
            }
        } else {
            ResponseEntity<R_OVER> res = restTemplate.exchange(url.toString(), httpMethod, getRequestEntity(command.getParameters(), token), resulTypetOverride);
            if(res.getStatusCode() == HttpStatus.OK){
                command.setStatus(CommandStatus.SUCCESS);
            } else {
                command.setStatus(CommandStatus.FAILURE);
            }
            return res.getBody();
        }
        return null;
    }

    public void failIfUnauthenticated(Credentials credentials) throws AuthenticationException {
        if (credentials.getToken() == null)
            throw new AuthenticationException("Not authenticated");
    }

    /**
     * @return the requestEntity
     */
    private <T> HttpEntity<T> getRequestEntity(T body, String token, MediaType...mediaTypes ) {

        if( null == mediaTypes || mediaTypes.length == 0 ) {
            mediaTypes = new MediaType[ 1 ];
            mediaTypes[ 0 ] = MediaType.APPLICATION_JSON;
        }

        if( mediaTypes.length > 1 ) {
            throw new IllegalArgumentException( "Should only be one MediaType here" );
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept( Arrays.asList(mediaTypes) );
        requestHeaders.add( "Accept-Encoding", "deflate" );
        requestHeaders.add( "Connection", "Close" );
        requestHeaders.add( "User-Agent", "tikinou-sd-api" );
        if(token != null)
            requestHeaders.add("token", token);
        return new HttpEntity<>(body,  requestHeaders );
    }


    public <R extends CommandResult> R handleError(Exception e, Class<R> returnType, R defaultValue){
        R result = defaultValue;
        if(e instanceof HttpClientErrorException){
            String s = ((HttpClientErrorException) e).getResponseBodyAsString();
            try {
              result = ModuleRegistration.getInstance().getConfiguredObjectMapper().readValue(s, returnType);
            } catch (IOException e1) {
                result.setMessage(e1.getMessage());
            }
        } else {
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
