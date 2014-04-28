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

import com.tikinou.schedulesdirect.commands.*;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.headend.GetHeadendsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.GetLineupDetailsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.GetSubscribedLineupsCommand;
import com.tikinou.schedulesdirect.core.commands.lineup.LineupManagementCommand;
import com.tikinou.schedulesdirect.core.commands.message.DeleteMessageCommand;
import com.tikinou.schedulesdirect.core.commands.program.GetProgramsCommand;
import com.tikinou.schedulesdirect.core.commands.schedules.GetSchedulesCommand;
import com.tikinou.schedulesdirect.core.commands.status.GetStatusCommand;
import com.tikinou.schedulesdirect.core.commands.token.TokenCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastien Astie.
 */
@Configuration
public class SchedulesDirectConfig {
    @Bean
    SchedulesDirectClient schedulesDirectClient(){
        return new SchedulesDirectClientImpl();
    }

    @Bean
    TokenCommand tokenCommand(){
        return new TokenCommandImpl();
    }

    @Bean
    GetStatusCommand getStatusCommand(){
        return new GetStatusCommandImpl();
    }

    @Bean
    GetLineupDetailsCommand getLineupDetailsCommand(){ return new GetLineupDetailsCommandImpl(); }

    @Bean
    GetSubscribedLineupsCommand getSubscribedLineupsCommand(){ return new GetSubscribedLineupsCommandImpl(); }

    @Bean
    GetProgramsCommand getProgramsCommand(){
        return new GetProgramsCommandImpl();
    }

    @Bean
    GetSchedulesCommand getSchedulesCommand(){
        return new GetSchedulesCommandImpl();
    }

    @Bean
    GetHeadendsCommand getHeadendsCommand(){
        return new GetHeadendsCommandImpl();
    }

    @Bean
    LineupManagementCommand addLineupCommand(){ return new AddLineupCommandImpl(); }

    @Bean
    LineupManagementCommand deleteLineupCommand(){ return new DeleteLineupCommandImpl(); }

    @Bean
    DeleteMessageCommand deleteMessageCommand(){
        return new DeleteMessageCommandImpl();
    }
}
