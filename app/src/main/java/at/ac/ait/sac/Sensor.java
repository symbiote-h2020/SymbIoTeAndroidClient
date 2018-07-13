package at.ac.ait.sac;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple data class for a SymbIoTe sensor
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 13.07.2018.   ¯\_(ツ)_/¯
 */
public class Sensor {

    private static final Logger LOG = LoggerFactory.getLogger(Sensor.class);

    public final String platformId;
    public final String platformName;
    public final String name;
    public final String id;
    public final String description;
    public final String locationName;
    public final Double latitude;
    public final Double longitude;


    /**
     * ugly constructor for the sake of simplicity
     * @param id
     * @param name
     * @param platformId
     * @param platformName
     * @param description
     * @param locationName
     * @param latitude
     * @param longitude
     */
    public Sensor(String id, String name, String platformId, String platformName, String description, String locationName, Double latitude, Double longitude) {
        LOG.debug("Creating sensor: {}",id);
        this.id = id;
        this.name = name;
        this.platformId = platformId;
        this.platformName = platformName;
        this.description = description;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Sensor{("+id+","+name+")@"+platformId+"}";
    }

    public static Collection<Sensor> createCollection(JSONArray jsonArray) throws JSONException {
        Collection<Sensor> sensors = new ArrayList<>();
        if (jsonArray != null){
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                try{
                    sensors.add(new Builder()
                            .setPlatformId(entry.getString(SymbIoTeConstants.PARAM_PLATFORM_ID))
                            .setPlatformName(entry.getString(SymbIoTeConstants.PARAM_PLATFORM_NAME))
                            .setName(entry.getString(SymbIoTeConstants.PARAM_SENSOR_NAME))
                            .setId(entry.getString(SymbIoTeConstants.PARAM_ID))
                            .setDescription(entry.getString(SymbIoTeConstants.PARAM_DESCRIPTION))
                            .setLocationName(entry.getString(SymbIoTeConstants.PARAM_LOCATION_NAME))
                            .setLatitude(entry.getDouble(SymbIoTeConstants.PARAM_LOCATION_LATITUDE))
                            .setLongitude(entry.getDouble(SymbIoTeConstants.PARAM_LOCATION_LONGITUDE))
                            .build());
                }catch (JSONException e){
                    LOG.error("Couldn't create a sensor from entry {}",entry.toString());
                }

            }
        }
        return sensors;
    }
    
    private static class Builder{

        private String platformId;
        private String platformName;
        private String name;
        private String id;
        private String description;
        private String locationName;
        private Double latitude;
        private Double longitude;

        public Sensor build() {
            return new Sensor(id, name, platformId, platformName, description, locationName, latitude, longitude);
        }

        public Builder setPlatformId(String platformId) {
            this.platformId = platformId;
            return this;
        }
        
        public Builder setPlatformName(String platformName) {
            this.platformName = platformName;
            return this;
        }

        public Builder setName(String name) {
            this.name=name;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setLocationName(String locationName) {
            this.locationName = locationName;
            return this;
        }

        public Builder setLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }
    }
}
