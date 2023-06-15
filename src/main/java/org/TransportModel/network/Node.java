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
}
