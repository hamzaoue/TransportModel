package org.TransportModel.Generation;

import com.vividsolutions.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;

public class Zone
{
    String id;
    Coordinate center;
    MultiPolygon shape;

    public Zone(String id, MultiPolygon multiPolygon)
    {
        this.id = id;
        this.shape = multiPolygon;
    }

    public String getId()
    {return this.id;}
 }
