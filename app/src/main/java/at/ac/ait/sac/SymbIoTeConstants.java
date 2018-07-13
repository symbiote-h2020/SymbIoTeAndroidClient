package at.ac.ait.sac;

/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by EdeggerK on 06.02.2018.   ¯\_(ツ)_/¯
 */

public interface SymbIoTeConstants {

    String CORE_AAM_SERVER_URL_DEFAULT = "https://symbiote-open.man.poznan.pl/coreInterface";
    //String CORE_AAM_SERVER_URL = "https://symbiote.man.poznan.pl/coreInterface";
    //private final static String AAMServerAddress = "https://symbiote-dev.man.poznan.pl/coreInterface";
    String SERVICE_CORE_AAM = "SymbIoTe_Core_AAM";
    String SERVICE_SEARCH = "search";
    String SERVICE_RESPONSE = "serviceResponse";
    String PLATFORM_ID = "ait_kiola";


    String PATH_QUERY = "query";
    String PARAM_SENSOR_NAME = "name";
    String PARAM_PLATFORM_ID = "platformId";
    //that's a different one as above :)
    String QUERY_PARAM_PLATFORM_ID = "platform_id";
    String PARAM_PLATFORM_NAME="platformName";
    String PARAM_OWNER="owner";
    String PARAM_ID="id";
    String PARAM_DESCRIPTION="description";
    String PARAM_LOCATION_NAME="locationName";
    String PARAM_LOCATION_LATITUDE="locationLatitude";
    String PARAM_LOCATION_LONGITUDE="locationLongitude";
    String PARAM_LOCATION_ALTITUDE="locationAltitude";
    String PARAM_OBSERVED_PROPERTIES="observedProperties";
    String PARAM_RESOURCE_TYPE="resourceType";
    String PARAM_RANKING= "ranking";
    String PARAM_BODY= "body";

    String PATH_RESOURCE_URL = "resourceUrls" ;
    String PATH_OBSERVATION = "Observations";
    String OBS_VALUES = "obsValues";
    String OBS_PROPERTY = "obsProperty";
    String DESCRIPTION = "description";
    String VALUE = "value";
    String SAMPLING_TIME = "sampling_time";
}
