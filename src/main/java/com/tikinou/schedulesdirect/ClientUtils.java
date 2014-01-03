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
import com.tikinou.schedulesdirect.core.Command;
import com.tikinou.schedulesdirect.core.CommandResult;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.domain.CommandStatus;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.exceptions.AuthenticationException;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

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
        prepareMessageConverters(restTemplate.getMessageConverters());
    }

    private void prepareMessageConverters(List<HttpMessageConverter<?>> converters){
        for( HttpMessageConverter<?> messageConverter : converters ) {
            if( messageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
                MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) messageConverter;
                mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
                mappingJackson2HttpMessageConverter.setObjectMapper( objectMapper );
            }
        }
    }

    public <R_IMPL extends R, P, R extends CommandResult, C extends Command<P,R>> void executeRequest(SchedulesDirectClient client, C command, P parameters, Class<R_IMPL> resultType){
        MultiValueMap<String,P> valueMap = new LinkedMultiValueMap<>();
        valueMap.add("request", parameters);
        HttpEntity<?> entity = getRequestEntity(valueMap);
        ResponseEntity<R_IMPL> res = restTemplate.postForEntity(client.getUrl(), entity, resultType);
        if(res.getStatusCode() == HttpStatus.OK){
            R result = res.getBody();
            command.setResults(result);
            command.setStatus(CommandStatus.SUCCESS);
        } else {
            command.setStatus(CommandStatus.FAILURE);
        }
    }

    public void failIfUnauthenticated(Credentials credentials) throws AuthenticationException {
        if (credentials.getRandhash() == null)
            throw new AuthenticationException("Not authenticated");
    }

    /**
     * @return the requestEntity
     */
    private <T> HttpEntity<T> getRequestEntity(T body, MediaType...mediaTypes ) {

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
}
