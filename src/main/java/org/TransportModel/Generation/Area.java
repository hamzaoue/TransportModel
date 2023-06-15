package org.TransportModel.Generation;

import java.util.HashMap;

public class Area
{
    HashMap<String,Zone> zones;
    public void addZone(Zone zone)
    {
        this.zones.put(zone.getId(),zone);
    }
}
