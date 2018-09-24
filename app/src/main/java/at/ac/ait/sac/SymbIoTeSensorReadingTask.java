package at.ac.ait.sac;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A task querying the desired platform sensor by using symbIoTe (resolving the platform URL via
 * a CRAM request first).
 *
 * Created by EdeggerK on 11.09.2017.   ¯\_(ツ)_/¯
 */

public class SymbIoTeSensorReadingTask extends AsyncTask<String,Void,String>{

    private static final Logger LOG = LoggerFactory.getLogger(SymbIoTeSensorReadingTask.class);
    //query parameter for the ODATA filter (number of returned measurements)
    private static final String LIMIT_NUMBER_OBSERVATIONS = "30";
    private final WeakReference<Context> mCtx;
    private final SensorReaderCallback mCallback;


    public SymbIoTeSensorReadingTask(Context ctx, SensorReaderCallback callback) {
        this.mCtx = new WeakReference<Context>(ctx);
        this.mCallback = callback;
    }

    @Override
    protected String doInBackground(String... sensorIds) {
        OkHttpClient client = NetworkUtil.createClient();
        //we must not call that on the main tread
        SymbIoTeCoreIntegration symbiote = new SymbIoTeCoreIntegration(mCtx.get());
        String result = null;
        //we are taking the first sensor
        String sensorId = sensorIds[0];
        Request.Builder cramRequest = new Request.Builder().url(symbiote.getCramUrl(sensorId).toString()).get();
        symbiote.addSecurityHeaders(cramRequest);
        Request cram = cramRequest.build();
        LOG.debug("GET request  {}",cram.url());
        Response cramResponse = null;
        Response rapResponse = null;
        try {
            cramResponse = client.newCall(cram).execute();
            if (cramResponse.isSuccessful()){
                String cramBody = Objects.requireNonNull(cramResponse.body()).string();
                LOG.debug("Body: "+cramBody);
                JSONObject jCram = new JSONObject(cramBody);
                JSONObject raps = jCram.getJSONObject(SymbIoTeConstants.PARAM_BODY);
                Uri rapUrl = Uri.parse(raps.getString(sensorId)).buildUpon().appendEncodedPath(SymbIoTeConstants.PATH_OBSERVATION).appendQueryParameter("$top", LIMIT_NUMBER_OBSERVATIONS).build();
                LOG.debug("RAP {}", rapUrl.toString());
                Request.Builder rapRequest = new Request.Builder().url(rapUrl.toString()).get();
                symbiote.addSecurityHeaders(rapRequest);
                rapResponse = client.newCall(rapRequest.build()).execute();
                LOG.debug("Rap: {} -> {}",rapResponse.request().url(),rapResponse.code());
                if (rapResponse.isSuccessful()){
                    result = rapResponse.body().string();
                }else{
                    throw new IOException("Unsuccessful response from RAP "+rapResponse.code());
                }
            }
        } catch (IOException e) {
            LOG.error("Couldn't execute CRAM request: {}",e);
        } catch (JSONException e) {
            LOG.warn("Ignoring invalid CRAM response: {}",e);
        } finally {
            try{
                Objects.requireNonNull(cramResponse).close();
            }catch (Exception e){
                LOG.warn("Couldn't close CRAM response");
            }
            try{
                Objects.requireNonNull(rapResponse).close();
            }catch (Exception e){
                LOG.warn("Couldn't close RAP response");
            }
        }
        LOG.debug("Response: {}",cramResponse);
        return result;
    }

    @Override
    protected void onPostExecute(String rapResponse) {
        super.onPostExecute(rapResponse);
        if (mCallback != null){
            if (TextUtils.isEmpty(rapResponse)){
                mCallback.onError(new IOException("Unsuccessful RAP response"));
            }else{
                mCallback.onSuccess(rapResponse);
            }
        }
    }

    public interface SensorReaderCallback{
        void onSuccess(String responseBody);
        void onError(Exception e);
    }

}
