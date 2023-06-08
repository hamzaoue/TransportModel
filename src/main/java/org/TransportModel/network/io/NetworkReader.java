package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
///////////////////////////////////////////////////////////////////////////////////////////////////
/**             NetworkReader class is used for reading and importing network data               */
///////////////////////////////////////////////////////////////////////////////////////////////////
public abstract class NetworkReader
{
    enum NODE_ATTRIBUTES {ID, X, Y}
    enum LINK_ATTRIBUTES {ID, FROM_ID, TO_ID, LENGTH, CAPACITY, SPEED, BIDIRECTIONAL,TIME}
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Extracts data from a file using the specified delimiter.
     @param filePath the path to the file to be read
     @param delimiter the character used to separate values in each line
     @return a list of string arrays containing the extracted data (1 line = 1 array) */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected List<String[]> extractData(String filePath, char delimiter)
    {
        List<String[]> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null)
                lines.add(line.split(String.valueOf(delimiter)));
        }catch (IOException e) {e.printStackTrace();}
        return lines;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Extracts data from a file using the specified delimiter.
     @param filePath the path to the file to be read
     @return a list of string (1 line = 1 String) */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected List<String> extractData(String filePath)
    {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = reader.readLine()) != null)
                lines.add(line);
        }catch (IOException e) {e.printStackTrace();}
        return lines;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Creates a list of links from a MultiLineString by transforming each linestring into a link
    * @param multiLineString the MultiLineString from which to create the links
    * @return a list of links created from the MultiLineString */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected List<Link> createLinksFromMultiLineString(MultiLineString multiLineString)
    {
        List<Link> links = new ArrayList<>();
        int numGeometries = multiLineString.getNumGeometries();
        for (int i = 0; i < numGeometries; i++)
            links.add(createLinkFromLineString((LineString) multiLineString.getGeometryN(i)));
        return links;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Creates a Link between the first and the last point of the LineString
     * The length of the Link is calculated based on the entire LineString segment
     * @param lineString the LineString from which to create the link
     * @return a Link object created from the LineString */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    protected Link createLinkFromLineString(LineString lineString)
    {
        Coordinate[] lineCoordinates = lineString.getCoordinates();
        Node fromNode = new Node(lineCoordinates[0]);
        Node toNode = new Node(lineCoordinates[lineCoordinates.length-1]);
        Link link = new Link(fromNode,toNode);
        link.setLengthInM((int)lineString.getLength());
        return link;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Adds links to a network based on the provided data
     * @param network      The network to which the links are added
     * @param dataLines    A list of string arrays representing the link data (1 line = 1 link)
     * @param headersIndex A HashMap containing the indexes of column headers in the link data */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public List<Link> createLinksFromData(Network network, List<String[]> dataLines, HashMap<LINK_ATTRIBUTES, Integer> headersIndex)
    {
        List<Link> links = new ArrayList<>();
        for (String[] dataLine : dataLines) {
            try {
                Link link;
                Node from_node = network.getNode(dataLine[headersIndex.get(LINK_ATTRIBUTES.FROM_ID)]);
                Node to_node = network.getNode(dataLine[headersIndex.get(LINK_ATTRIBUTES.TO_ID)]);
                if (headersIndex.containsKey(LINK_ATTRIBUTES.ID))
                    link = new Link(dataLine[headersIndex.get(LINK_ATTRIBUTES.ID)], from_node, to_node);
                else
                    link = new Link(from_node, to_node);
                if (headersIndex.containsKey(LINK_ATTRIBUTES.BIDIRECTIONAL))
                    link.setBidirectional(dataLine[headersIndex.get(LINK_ATTRIBUTES.TO_ID)].equals("1"));
                if (headersIndex.containsKey(LINK_ATTRIBUTES.LENGTH))
                    link.setLengthInM((int)(Double.parseDouble(dataLine[headersIndex.get(LINK_ATTRIBUTES.LENGTH)])));
                if (headersIndex.containsKey(LINK_ATTRIBUTES.SPEED))
                    link.setNormalSpeedInKMH(Integer.valueOf(dataLine[headersIndex.get(LINK_ATTRIBUTES.SPEED)]));
                if (headersIndex.containsKey(LINK_ATTRIBUTES.TIME))
                    link.setNormalTimeInS(Integer.valueOf(dataLine[headersIndex.get(LINK_ATTRIBUTES.TIME)]));
                if (headersIndex.containsKey(LINK_ATTRIBUTES.CAPACITY))
                    link.setCapacityPerHour(Integer.valueOf(dataLine[headersIndex.get(LINK_ATTRIBUTES.CAPACITY)]));
                links.add(link);
            }
            catch (Exception e) {e.printStackTrace();}
        }
        return links;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Adds nodes to a network based on the provided data
     * @param dataLines    A list of string arrays representing the node data (1 line = 1 node)
     * @param headersIndex A HashMap containing the indexes of column headers in the node data */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public List<Node> createNodesFromData(List<String[]> dataLines, HashMap<NODE_ATTRIBUTES, Integer> headersIndex)
    {
        List<Node> nodes = new ArrayList<>();
        for (String[] dataLine : dataLines) {
            try {
                Node node;
                double x = Double.parseDouble(dataLine[headersIndex.get(NODE_ATTRIBUTES.X)]);
                double y = Double.parseDouble(dataLine[headersIndex.get(NODE_ATTRIBUTES.Y)]);
                if (headersIndex.containsKey(NODE_ATTRIBUTES.ID))
                    node = new Node(dataLine[headersIndex.get(NODE_ATTRIBUTES.ID)], new Coordinate(x, y));
                else
                    node = new Node(new Coordinate(x, y));
                nodes.add(node);
            } catch(Exception e){e.printStackTrace();}
        }
        return nodes;
    }
}