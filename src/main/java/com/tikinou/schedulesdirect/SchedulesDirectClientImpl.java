package com.tikinou.schedulesdirect;

import com.tikinou.schedulesdirect.commands.RandHashCommandImpl;
import com.tikinou.schedulesdirect.core.AbstractSchedulesDirectClient;
import com.tikinou.schedulesdirect.core.Command;
import com.tikinou.schedulesdirect.core.commands.randhash.RandHashCommand;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImpl extends AbstractSchedulesDirectClient{

    @Override
    public <T extends Command<?,?>> T createCommand(Class<T> commandClass) {
        return (T) new RandHashCommandImpl();
    }
}
