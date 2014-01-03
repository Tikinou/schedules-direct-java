package com.tikinou.schedulesdirect;

import com.tikinou.schedulesdirect.commands.RandHashCommandImpl;
import com.tikinou.schedulesdirect.core.AbstractSchedulesDirectClient;
import com.tikinou.schedulesdirect.core.Command;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.commands.randhash.RandHashCommand;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImpl extends AbstractSchedulesDirectClient {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public <T extends Command<?,?>> T createCommand(Class<T> commandClass) {
        return applicationContext.getBean(commandClass);
    }
}
