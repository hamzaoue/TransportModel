package org.TransportModel.network;

import org.locationtech.jts.geom.Coordinate;
///////////////////////////////////////////////////////////////////////////////////////////////////
/**                   Node class represents a node in the transportation network                 */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class Node
{
    final private String id;
    final private Coordinate coordinate;
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                        Constructor                                           */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public Node(String id, Coordinate coordinate)
    {
        this.id = id;
        this.coordinate = coordinate;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                          Getters                                             */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public Coordinate getCoordinate(){return this.coordinate;}
    public String getId(){return this.id;}
    public double getDistanceInM(Node node)
    {
        double EARTH_RADIUS = 6371000;
        double lat1 = Math.toRadians(node.getCoordinate().x);
        double lon1 = Math.toRadians(node.getCoordinate().y);
        double lat2 = Math.toRadians(this.getCoordinate().x);
        double lon2 = Math.toRadians(this.getCoordinate().y);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
