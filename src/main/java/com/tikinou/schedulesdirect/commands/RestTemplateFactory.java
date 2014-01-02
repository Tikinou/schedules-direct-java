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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tikinou.schedulesdirect.core.domain.ResponseCode;
import com.tikinou.schedulesdirect.core.jackson.ModuleRegistration;
import com.tikinou.schedulesdirect.core.jackson.converters.ResponseCodeConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sebastien Astie
 */
public class RestTemplateFactory {
    public static RestTemplate getRestTemplate(){
        RestTemplate template = new RestTemplate();
        prepareMessageConverters(template.getMessageConverters());
        return template;
    }

    private static void prepareMessageConverters(List<HttpMessageConverter<?>> converters){
        for( HttpMessageConverter<?> messageConverter : converters ) {
            if( messageConverter instanceof MappingJackson2HttpMessageConverter) {
                ObjectMapper objectMapper = ModuleRegistration.getInstance().getConfiguredObjectMapper();
                MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) messageConverter;
                mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_HTML));
                mappingJackson2HttpMessageConverter.setObjectMapper( objectMapper );
            }
        }
    }
}
