/**
 *
 */
package com.kristou.urgLibJ.RangeSensor;

/**
 * @author Administrator
 *
 */
public class RangeSensorInternalInformation {

    enum tag {

        MODL, // !< Product model
        LASR, // !< Laser status On OR Off
        SCSP, // !< Motor desired speed [rpm]
        MESM, // !< Sensor state ID with title
        SBPS, // !< Communication type
        TIME, // !< Sensor time 6 bits Hex
        STAT // !< Sensor stuation "stable/unstable"
    };

    public String model; // !< MODL
    public String laserStatus; // !< LASR
    public String motorDesiredSpeed; // !< SCSP
    public String stateID; // !< MESM
    public String communicationType; // !< SBPS
    public String internalTime; // !< TIME
    public String sensorSituation; // !< STAT
}
