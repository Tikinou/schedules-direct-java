package com.tikinou.schedulesdirect;

import com.tikinou.schedulesdirect.core.AbstractSchedulesDirectClient;
import com.tikinou.schedulesdirect.core.ParameterizedCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImpl extends AbstractSchedulesDirectClient {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public <T extends ParameterizedCommand<?,?>> T createCommand(Class<T> commandClass) {
        return applicationContext.getBean(commandClass);
    }
}
