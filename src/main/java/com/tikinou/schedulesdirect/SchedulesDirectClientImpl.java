package com.tikinou.schedulesdirect;

import com.tikinou.schedulesdirect.core.Command;
import com.tikinou.schedulesdirect.core.SchedulesDirectClient;
import com.tikinou.schedulesdirect.core.domain.*;
import com.tikinou.schedulesdirect.core.exceptions.AuthenticationException;
import com.tikinou.schedulesdirect.core.exceptions.VersionNotSupportedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Sebastien Astie
 */
public class SchedulesDirectClientImpl implements SchedulesDirectClient{
    private final static String DEFAUlT_BASE_URL = "https://data2.schedulesdirect.org/";
    private final static String DEFAUlT_BETA_BASE_URL = "http://23.21.174.111/";
    private final static String DEFAULT_ENDPOINT = "handleRequest.php";
    private final static Log LOG = LogFactory.getLog(SchedulesDirectClient.class);

    private SchedulesDirectApiVersion apiVersion;
    private Credentials credentials;
    private String baseUrl;
    private String endPoint;

    @Override
    public void setup(SchedulesDirectApiVersion apiVersion, boolean useBetaService) throws VersionNotSupportedException {
        this.apiVersion = apiVersion;
        if(useBetaService)
            baseUrl = DEFAUlT_BETA_BASE_URL;
        else
            baseUrl = DEFAUlT_BASE_URL;
        endPoint = DEFAULT_ENDPOINT;
        if(LOG.isDebugEnabled()){
            LOG.debug("Setting up with API version: " + apiVersion + ", baseUrl: '" + baseUrl + "', endPoint: " + endPoint);
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getDefaultBaseUrl() {
        return DEFAUlT_BASE_URL;
    }

    @Override
    public String getDefaultEndpoint() {
        return DEFAULT_ENDPOINT;
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }


    @Override
    public void connect(Credentials credentials, boolean forceConnect) throws AuthenticationException {
        connect(credentials, null, forceConnect);
    }

    @Override
    public void connect(Credentials credentials, String baseUrl, boolean forceConnect) throws AuthenticationException {
        connect(credentials, baseUrl, null, forceConnect);
    }

    @Override
    public void connect(Credentials credentials, String baseUrl, String endPoint, boolean forceConnect) throws AuthenticationException {
        if (credentials == null)
            throw new AuthenticationException("credentials object cannot be null");
        if(endPoint != null)
            this.endPoint = endPoint;
        if(baseUrl != null)
            this.baseUrl = baseUrl;

        if(LOG.isDebugEnabled()){
            LOG.debug("Connecting with credentials:" + credentials.toString() + " forceConnect: " + forceConnect + ", baseUrl: '" + baseUrl + "', endPoint: " + endPoint);
        }

        if(!forceConnect){
            if (this.credentials != null) {
                //are these the same credentials ?
                if (this.credentials.sameUserNamePassword(credentials)) {
                    // is the randhash older than 12 hours ?
                    if (!this.credentials.isOlderThan(CREDENTIALS_EXPIRY_HOURS)) {
                        if(LOG.isInfoEnabled())
                            LOG.info("credentials less than " + CREDENTIALS_EXPIRY_HOURS + " hours. No need to get a new randhash")
                        return;
                    }
                }
            } else if(!credentials.isOlderThan(CREDENTIALS_EXPIRY_HOURS)){
                this.credentials = credentials;
                return;
            }
        }
        // if we got here we need to get a new randhash
        this.credentials = credentials;
        assert this.credentials.getUsername() != null;
        assert this.credentials.getPassword() != null;
        Command cmd = getCommand(ActionType.GET, ObjectTypes.RANDHASH)
        execute(cmd)
        if (cmd.status != CommandStatus.SUCCESS)
            throw new AuthenticationException("Could not login to schedules direct", ResponseCode.fromCode(cmd.results.code))
    }

    @Override
    public void execute(Command command) {

    }

    @Override
    public Command createCommand(ActionType actionType, ObjectTypes objectType) {
        return null;
    }
}
