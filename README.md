# SymbIoTe Android Client

An Android client examplifying some basic SymbIoTe integration workflows like searching for certain sensors on specific platforms and retrieving the respective data.

It is mainly intented to be a starting point for your own SymbIoTe development doing more exciting stuff. I may help you by introducing some simple use cases accessing the basic SymbIoTe core infrastructure and your own platform dependent components.

## Components
* **[SymbIoTeClientActivity](https://git-service.ait.ac.at/sim-symbiote/SymbIoTe-Android-Client/blob/master/app/src/main/java/at/ac/ait/sac/SymbIoTeClientActivity.java)** - The main entry point for this demo

* **[SymbIoTeCoreSensorQueryTask](https://git-service.ait.ac.at/sim-symbiote/SymbIoTe-Android-Client/blob/master/app/src/main/java/at/ac/ait/sac/SymbIoTeCoreSensorQueryTask.java)** - A background task to query the SymbIoTe core for registered sensors from various platforms

* **[SymbIoTeSensorReadingTask](https://git-service.ait.ac.at/sim-symbiote/SymbIoTe-Android-Client/blob/master/app/src/main/java/at/ac/ait/sac/SymbIoTeSensorReadingTask.java)** - A background task to retrieve the actual sensor data from the target platform via SymbIoTe 

For a complete description of all the components, please check the [JavaDocs](https://github.com/symbiote-h2020/SymbIoTeAndroidClient/blob/master/javadoc/index.html).

## SymbIoTe dependency

To use SymbIoTe in your own project just add the following dependency to your app level [build.gradle](https://git-service.ait.ac.at/sim-symbiote/SymbIoTe-Android-Client/blob/master/app/build.gradle):

```json
    //SymbIoTe security client
    implementation 'com.github.symbiote-h2020:SymbIoTeSecurity4Android:25.6.0'
```
