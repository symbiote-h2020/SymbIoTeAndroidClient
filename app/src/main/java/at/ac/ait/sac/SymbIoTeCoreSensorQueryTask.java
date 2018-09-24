package at.ac.ait.sac;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Task performing symbiote core searches. You can provide a platform ID when executing the task
 * and you will get a list of registered sensors.
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 06.02.2018.   ¯\_(ツ)_/¯
 */

class SymbIoTeCoreSensorQueryTask extends AsyncTask<String, Void, Collection<Sensor>> {

    private static final Logger LOG = LoggerFactory.getLogger(SymbIoTeCoreSensorQueryTask.class);

    /**
     * this sensorName will be used, if the beacon AA:BB:CC:DD:EE:FF
     * (internal id 6393cfc4ab6d416690b21278957aeab2) is detected...
     */
    private final static String MOCK_SENSOR_NAME= "25bfe4ebdc4f14a519af6c7fdee1f1e7";
    private final QueryTaskCallback mCallback;
    private final WeakReference<Context> mCtx;


    public SymbIoTeCoreSensorQueryTask(@NonNull Context ctx, QueryTaskCallback callback) {
        this.mCallback = callback;
        this.mCtx = new WeakReference<>(ctx);
    }


    @Override
    protected Collection<Sensor> doInBackground(String... platformIds) {
        String platformId = platformIds[0];
        //must not be called on the main thread -> network
        SymbIoTeCoreIntegration symbiote = new SymbIoTeCoreIntegration(mCtx.get().getApplicationContext());
        OkHttpClient client = NetworkUtil.createClient();
        Response queryResponse = null;
        ResponseBody body = null;
        @SuppressWarnings("unchecked") Collection<Sensor> result = Collections.EMPTY_LIST;
        try {
            Request.Builder coreRequest = new Request.Builder().url(symbiote.getQueryUrl(platformId).toString()).get();
            symbiote.addSecurityHeaders(coreRequest);
            Request queryRequest = coreRequest.build();
            queryResponse = client.newCall(queryRequest).execute();
            if (queryResponse.isSuccessful()){
                body = queryResponse.body();
                LOG.debug("Body: "+body);
                if (body != null){
                    JSONObject jBody = new JSONObject(body.string());
                    boolean isVerified = symbiote.isResponseVerified(jBody,
                            SymbIoTeConstants.SERVICE_SEARCH, SymbIoTeConstants.SERVICE_CORE_AAM);
                    if (!isVerified){
                        LOG.warn("**** Search queryResponse could not be verified! - Maybe the core got compromised! >:/");
                    }
                    /*
                     * right now, we just issue a warning, if the queryResponse couldn't be verified
                     */
                    //noinspection PointlessBooleanExpression
                    if (true || isVerified){
                        JSONArray sensors = jBody.getJSONArray(SymbIoTeConstants.PARAM_BODY);
                        result = Sensor.createCollection(sensors);
                    }
                }
            }else{
                LOG.warn("Core query could not be completed successful {} -> {}",queryResponse.request().url(), queryResponse.code());
            }
        } catch (IOException | JSONException e) {
            LOG.error("Couldn't get list of sensors for platform",e);
        } finally {
            if (body != null){
                try{
                    body.close();
                }catch (Exception e){
                    LOG.warn("Couldn't close queryResponse body ",e);
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Collection<Sensor> sensorId) {
        super.onPostExecute(sensorId);
        if (mCallback != null){
            mCallback.onSearchComplete(sensorId);
        }else{
            LOG.warn("Dropping result on the floor - no callback available");
        }
    }

    /**
     * Interface to be called at the end of a core search task. Implement it to get informed about
     * the result.
     */
    public interface QueryTaskCallback{
        /**
         * Will be called at the end of the core search
         * @param sensorId a collection containing all the found sensors (may be empty)
         */
        void onSearchComplete(Collection<Sensor> sensorId);
        void onError(Exception e);
    }

}
