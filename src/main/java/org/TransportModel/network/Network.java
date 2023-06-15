package org.TransportModel.network;

import java.util.HashMap;

///////////////////////////////////////////////////////////////////////////////////////////////////
/**    Network class represents a transportation network containing a graph of nodes and links   */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class Network
{
    private final HashMap<String,Node> nodes;
    private final HashMap<String,Link> links;
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                       Constructor                                            */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public Network(){
        this.nodes = new HashMap<>();
        this.links = new HashMap<>();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                        Getters                                               */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean containsNode(String id){return this.nodes.containsKey(id);}
    public boolean containsLink(String id){return this.nodes.containsKey(id);}
    public Node getNode(String id) {return this.nodes.get(id);}
    public HashMap<String,Node> getNodes(){return this.nodes;}
    public Link getLink(String id){return this.links.get(id);}
    public HashMap<String,Link> getLinks(){return this.links;}
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                                                                              */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void addNode(Node node) {
        //If node id already exists, do nothing
        if(!this.containsNode(node.getId()))
            this.nodes.put(node.getId(),node);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                                                                              */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void addLink(Link link) {
        //Check if from node already exits
        if(!this.containsNode(link.getFromNode().getId()))
            this.addNode(link.getFromNode());
        else
            link.setFromNode(this.getNode(link.getFromNode().getId()));
        //Check if to node already exists
        if(!this.containsNode(link.getToNode().getId()))
            this.addNode(link.getToNode());
        else
            link.setToNode(this.getNode(link.getToNode().getId()));
        //If link id already exists, replace it
        this.links.put(link.getId(),link);
    }
}
