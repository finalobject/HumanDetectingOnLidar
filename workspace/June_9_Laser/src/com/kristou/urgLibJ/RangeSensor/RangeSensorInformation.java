/**
 *
 */
package com.kristou.urgLibJ.RangeSensor;

/**
 * @author Administrator
 *
 */
public class RangeSensorInformation {

    enum tag {

        VEND, // !< Vendor Information
        PROD, // !< Product Information
        FIRM, // !< Firmware Version
        PROT, // !< Firmware Version
        SERI // !< Sensor Serial Number
    };

    public String vendor; // !< VEND
    public String product; // !< PROD
    public String firmware; // !< FIRM
    public String protocol; // !< PROT
    public String serial_number; // !< SERI
}
