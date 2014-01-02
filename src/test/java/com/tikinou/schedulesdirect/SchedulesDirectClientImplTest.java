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

import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.domain.Credentials;
import com.tikinou.schedulesdirect.core.domain.SchedulesDirectApiVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImplTest {
    private SchedulesDirectClient client;

    @Before
    public void setUp() throws Exception {
        client = new SchedulesDirectClientImpl();
        client.setup(SchedulesDirectApiVersion.VERSION_20130709, false);
    }

    @Test
    public void testConnect() throws Exception {
        Credentials credentials = new Credentials();
        client.connect(credentials, true);
    }
}
