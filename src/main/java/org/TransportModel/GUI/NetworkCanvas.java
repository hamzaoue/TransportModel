package org.TransportModel.GUI;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;
import org.jgrapht.Graph;

import javax.swing.*;
import java.awt.*;

///////////////////////////////////////////////////////////////////////////////////////////////////
/**                        Représentation graphique d'un réseau                                  */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class NetworkCanvas extends JComponent
{
    private Double[] bounds;
    private Network network;
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                          Constructor                                       */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public NetworkCanvas(Network network)
    {
        this.network = network;this.setupBounds();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                          Modificateurs                                       */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void setNetwork(Network network) {this.network = network;this.setupBounds();}
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                  Calcul les limites de coordonnés pour la mise à l'échelle                   */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void setupBounds()
    {
        this.bounds = new Double[]{null, null, null, null};
        for (Node node : this.network.getNodes().values()) {
            bounds[0] = (bounds[0] == null || bounds[0] > node.getCoordinate().x) ? node.getCoordinate().x : bounds[0];
            bounds[1] = (bounds[1] == null || bounds[1] > node.getCoordinate().y) ? node.getCoordinate().y : bounds[1];
            bounds[2] = (bounds[2] == null || bounds[2] < node.getCoordinate().x) ? node.getCoordinate().x : bounds[2];
            bounds[3] = (bounds[3] == null || bounds[3] < node.getCoordinate().y) ? node.getCoordinate().y : bounds[3];
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                   PaintComponent                                             */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void paintComponent(Graphics g)
    {
        if (this.network != null && this.bounds[0]!=null) {
            this.drawNodes(g);
            this.drawEdges(g);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                     Dessine les noeuds                                      */
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawNodes(Graphics g)
    {
        g.setColor(Color.BLUE);
        double screenWidth = this.getWidth(), screenHeight = this.getHeight();
        double range = Math.max(this.bounds[2] - this.bounds[0], this.bounds[3] - this.bounds[1]);
        for(Node node: this.network.getNodes().values())
        {
            int x = (int)((node.getCoordinate().x - bounds[0]) * screenWidth / range);
            int y = (int)((node.getCoordinate().y - bounds[1]) * screenHeight / range);
            g.fillOval(x,y,2,2);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                     Dessine les arcs                                        */
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private void drawEdges(Graphics g)
    {
        g.setColor(Color.red);
        double screenWidth = this.getWidth(), screenHeight = this.getHeight();
        double range = Math.max(this.bounds[2] - this.bounds[0], this.bounds[3] - this.bounds[1]);
        for (Link link : this.network.getLinks().values())
        {
            Node from = link.getFromNode();
            Node to = link.getToNode();
            int x1 = (int)((from.getCoordinate().x - bounds[0]) * screenWidth / range);
            int y1 = (int)((from.getCoordinate().y - bounds[1]) * screenHeight / range);
            int x2 = (int)((to.getCoordinate().x - bounds[0]) * screenWidth / range);
            int y2 = (int)((to.getCoordinate().y - bounds[1]) * screenHeight / range);
            g.drawLine(x1, y1, x2, y2);
        }
    }
}
