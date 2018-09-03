package com.kristou.urgLibJ.RangeSensor.Capture;

import com.kristou.urgLibJ.Connection.Connection;

public class CaptureFactory {

    public static Capture makeCapture(CaptureSettings s, Connection c) {
        switch (s.type) {
            case GD_Capture_mode: {
                Capture cap = (Capture) new GD_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case GE_Capture_mode: {
                Capture cap = (Capture) new GE_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case MD_Capture_mode: {
                Capture cap = (Capture) new MD_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case ME_Capture_mode: {
                Capture cap = (Capture) new ME_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case HD_Capture_mode: {
                Capture cap = (Capture) new HD_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case HE_Capture_mode: {
                Capture cap = (Capture) new HE_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case ND_Capture_mode: {
                Capture cap = (Capture) new ND_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            case NE_Capture_mode: {
                Capture cap = (Capture) new NE_Capture(c);
                cap.setSettings(s);
                return cap;
            }
            default:
                break;
        }
        return null;
    }
}
