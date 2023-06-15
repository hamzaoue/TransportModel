package org.TransportModel.network;
///////////////////////////////////////////////////////////////////////////////////////////////////
/**     Link class represents a connection between two nodes in the transportation network       */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class Link
{
    //Essentials
    final String id;
    private Node fromNode, toNode;
    //Optional
    private boolean isBidirectional;
    private double lengthInM, normalSpeedInMS, capacityPerHour;
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                        Constructor                                           */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public Link(String id, Node fromNode, Node toNode, boolean bidirectional, double speed, double capacity, double length)
    {
        this.id = id;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.isBidirectional = bidirectional;
        this.normalSpeedInMS = speed;
        this.capacityPerHour = capacity;
        this.lengthInM = length;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                          Getters                                             */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean isBidirectional(){return this.isBidirectional;}
    public double getNormalSpeedInMS(){return this.normalSpeedInMS;}
    public double getLengthInM(){return this.lengthInM;}
    public double getCapacityPerHour(){return this.capacityPerHour;}
    public Node getFromNode(){return this.fromNode;}
    public Node getToNode(){return this.toNode;}
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                          Setters                                             */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void setBidirectional(boolean isBidirectional){this.isBidirectional = isBidirectional;}
    public void setNormalSpeedInMS(double speed){this.normalSpeedInMS = speed;}
    public void setLengthInM(double length){this.lengthInM = length;}
    public void setCapacityPerHour(double capacityPerHour){this.capacityPerHour = capacityPerHour;}
    public void setFromNode(Node node){this.fromNode = node;}
    public void setToNode(Node node){this.toNode = node;}
    public String getId(){return this.id;}
}
