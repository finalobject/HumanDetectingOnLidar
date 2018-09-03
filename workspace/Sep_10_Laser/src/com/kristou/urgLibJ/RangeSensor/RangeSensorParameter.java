/**
 *
 */
package com.kristou.urgLibJ.RangeSensor;

/**
 * @author Administrator
 *
 */
public class RangeSensorParameter {

    enum tag {

        MODL, // !< Sensor model information
        DMIN, // !< Least possible measurement range [mm]
        DMAX, // !< Maximum possible measurement range [mm]
        ARES, // !< Angular resolution(Number of partitions in 360 degree)
        AMIN, // !< Least possible measurement direction in terms of angle
        AMAX, // !< Maximum possible measurement direction in terms of angle
        AFRT, // !< Front direction index
        SCAN // !< Standard angular velocity [rpm]
    };

    public String model; // !< MODL
    public long distance_min; // !< DMIN
    public long distance_max; // !< DMAX
    public int area_total; // !< ARES
    public int area_min; // !< AMIN
    public int area_max; // !< AMAX
    public int area_front; // !< AFRT
    public int scan_rpm; // !< SCAN
}
