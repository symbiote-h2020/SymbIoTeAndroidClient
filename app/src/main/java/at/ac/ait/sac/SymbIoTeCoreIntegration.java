package at.ac.ait.sac;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import at.ac.ait.sac.settings.Settings;
import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.AAMException;
import eu.h2020.symbiote.security.commons.exceptions.custom.JWTCreationException;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.AAMClient;
import eu.h2020.symbiote.security.communication.IAAMClient;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import eu.h2020.symbiote.security.helpers.MutualAuthenticationHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * Helper to talk to the core - must not be called from the main thread (is using network)
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 07.02.2018.   ¯\_(ツ)_/¯
 */

class SymbIoTeCoreIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(SymbIoTeCoreIntegration.class);
    private final OkHttpClient mClient;
    private final ISecurityHandler mClientSH;
    private final IAAMClient mAamClient;

    /**
     * this keystore will be created in the private app directory when talking to the core AAM
     */
    private final static String KEYSTORE_NAME = "certificates.jks";
    private final static String KEYSTORE_PASS = "kfdsaueroue%&/laiu87865a6";
    private final String mCoreAamUrl;

    public SymbIoTeCoreIntegration(@NonNull Context ctx){
        mCoreAamUrl = Settings.getCoreAAm(ctx);//SymbIoTeConstants.CORE_AAM_SERVER_URL_DEFAULT;
        mAamClient = new AAMClient(mCoreAamUrl);
        mClient = NetworkUtil.createClient();
        File keystore = new File(ctx.getApplicationContext().getFilesDir(),KEYSTORE_NAME);
        ISecurityHandler clientSH = null;
        try {
            clientSH = ClientSecurityHandlerFactory.getSecurityHandler(
                    mCoreAamUrl, keystore.getAbsolutePath(),KEYSTORE_PASS);
        } catch (SecurityHandlerException e) {
            LOG.error("Couldn't create client security handler: {}",e);
        }
        mClientSH = clientSH;

    }


    public Uri getRapUrl(String sensorId) throws SymbIoTeException {
        if (TextUtils.isEmpty(sensorId)){
            throw new IllegalArgumentException("Must provide a sensorId to resolve a RAP url for");
        }
        Uri rapUrl = null;
        IAAMClient restClient = new AAMClient(mCoreAamUrl);
        LOG.debug("SymbIoTeCoreIntegration client: {}",restClient);
        Uri coreCram = Uri.parse(mCoreAamUrl).buildUpon()
                .appendEncodedPath(SymbIoTeConstants.PATH_RESOURCE_URL)
                .appendQueryParameter(SymbIoTeConstants.PARAM_ID, sensorId)
                .build();
        Request.Builder cramRequest = new Request.Builder().url(coreCram.toString()).get();
        addSecurityHeaders(cramRequest);
        Request cram = cramRequest.build();
        LOG.debug("GET CRAM  {}",cram.url());
        Response cramResponse = null;
        ResponseBody cramBody = null;
        try {
            cramResponse = mClient.newCall(cram).execute();
            LOG.debug("Response: {}",cramResponse);
            if (cramResponse.isSuccessful()) {
                cramBody = cramResponse.body();
                if (cramBody != null){
                    LOG.debug("Body: " + cramBody);
                    JSONObject jCram = new JSONObject(cramBody.string());
                    JSONObject racps = jCram.getJSONObject(SymbIoTeConstants.PARAM_BODY);
                    rapUrl = Uri.parse(racps.getString(sensorId)).buildUpon().appendEncodedPath(SymbIoTeConstants.PATH_OBSERVATION).build();
                    LOG.debug("RAP {}", rapUrl.toString());
                }
            }
        } catch (IOException | JSONException e) {
            throw new SymbIoTeException("Couldn't get RAP url for sensor '"+sensorId+"'",e);
        } finally{
            if (cramBody != null){
                try{
                    cramBody.close();
                }catch (Exception e){
                    LOG.error("Couldn't close cram response body",e);
                }
            }
        }
        return rapUrl;

    }

    public void addSecurityHeaders(Request.Builder coreRequest) {
        String guestToken;
        try {
            guestToken = mAamClient.getGuestToken();
            LOG.debug("Adding security headers for guest token: {}", guestToken);
            SecurityRequest securityRequest = new SecurityRequest(guestToken);
            Map<String, String> securityHeaders = securityRequest.getSecurityRequestHeaderParams();
            // converting the prepared request into communication ready HTTP headers.
            for (String k : securityHeaders.keySet()) {
                //LOG.debug("Adding header: {}:'{}'",k,securityHeaders.get(k));
                coreRequest.addHeader(k, securityHeaders.get(k));
            }
        } catch (JWTCreationException | JsonProcessingException | AAMException e) {
            LOG.error("Got invalid security headers. This will lead to an unauthorized request!",e);
        }
    }


    public boolean isResponseVerified(JSONObject coreResponse, String serviceSearch, String aam) {
        boolean isResponseVerified = false;
        if (coreResponse != null && mClientSH != null){
            try {
                String jwt = coreResponse.getString(SymbIoTeConstants.SERVICE_RESPONSE);
                isResponseVerified = MutualAuthenticationHelper.isServiceResponseVerified(jwt,mClientSH
                        .getComponentCertificate(SymbIoTeConstants.SERVICE_SEARCH, SymbIoTeConstants.SERVICE_CORE_AAM));
            } catch (JSONException | CertificateException | SecurityHandlerException | NoSuchAlgorithmException e) {
                LOG.error("Couldn't verify server response: {}",e);
            }
        }
        return isResponseVerified;
    }

    public Uri getQueryUrl(String sensorName) {
        //see https://colab.intracom-telecom.com/pages/viewpage.action?pageId=7438840 for
        //see https://github.com/symbiote-h2020/SymbioteCloud/wiki/3.2-Search-for-resources
        Uri coreQuery = Uri.parse(mCoreAamUrl).buildUpon()
                .appendEncodedPath(SymbIoTeConstants.PATH_QUERY)
                .appendQueryParameter(SymbIoTeConstants.QUERY_PARAM_PLATFORM_ID, sensorName)
                .build();
        LOG.debug("Core query: {}",coreQuery.toString());
        return coreQuery;
    }

    public Uri getCramUrl(String sensorId) {
        return Uri.parse(mCoreAamUrl).buildUpon()
                .appendEncodedPath(SymbIoTeConstants.PATH_RESOURCE_URL)
                .appendQueryParameter(SymbIoTeConstants.PARAM_ID, sensorId)
                .build();
    }
}
