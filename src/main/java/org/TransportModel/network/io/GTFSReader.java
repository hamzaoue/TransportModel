package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////////////////////////
/** GTFSReader is a class that reads GTFS (General Transit Feed Specification) and fill a network */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class GTFSReader extends NetworkReader
{
    private static final String STOP_TIMES_FILE = "/stop_times.txt";
    private static final String STOPS_FILE = "/stops.txt";
    private static final String ROUTE_SECTIONS_FILE = "/route_sections.txt";
    private static final String PATHWAYS_FILE = "/pathways.txt";
    private static final String TRANSFERS_FILE = "/transfers.txt";
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads a GTFS folder and fill the network with data
     @param network the network to fill
     @param folderPath the path to the GTFS folder
     @throws FileNotFoundException if a required GTFS file is not found */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readGTFSFolder(Network network, String folderPath) throws FileNotFoundException
    {
        //If Needed stop_times file don't exist, error
        if(!new File(folderPath + STOP_TIMES_FILE).exists())
            throw new FileNotFoundException("Needed file " + STOP_TIMES_FILE + " not found");

        //If Needed stops file don't exist, error
        if(!new File(folderPath + STOPS_FILE).exists())
            throw new FileNotFoundException("Needed file " + STOPS_FILE + " not found");
        this.readStopFile(network, folderPath);

        //If Optional route_sections file don't exist, create it from Needed stop_times file
        if(!new File(folderPath + ROUTE_SECTIONS_FILE).exists())
            this.readStopTimesFile(folderPath);
        this.readRouteSectionsFile(network,folderPath);

        //If Optional pathways file don't exist, do what ?
        if (new File(folderPath + PATHWAYS_FILE).exists())
            this.readPathwayFile(network, folderPath);

        //If Optional transfers file don't exist, do what ?
        if (new File(folderPath + TRANSFERS_FILE).exists())
            this.readTransfersFile(network, folderPath);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads the stops file from the GTFS folder and creates nodes in the network based on the data.
     @param network the network to populate with nodes
     @param folderPath the path to the GTFS folder
     @throws RuntimeException if the stops file is missing required headers */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void readStopFile(Network network, String folderPath)
    {
        //Extract File data
        String filePath = folderPath + STOPS_FILE;
        List<String[]> dataLines = this.extractData(filePath, ',');
        List<String> headers = Arrays.asList(dataLines.remove(0));

        //If file don't contain needed columns, error
        if(!headers.contains("stop_id") || !headers.contains("stop_lon") || !headers.contains("stop_lat"))
            throw new RuntimeException("missing header in" + STOPS_FILE);

        //Associate Node attributes with data column index
        HashMap<NODE_ATTRIBUTES, Integer> headersIndex = new HashMap<>();
        headersIndex.put(NODE_ATTRIBUTES.ID, headers.indexOf("stop_id"));
        headersIndex.put(NODE_ATTRIBUTES.X, headers.indexOf("stop_lon"));
        headersIndex.put(NODE_ATTRIBUTES.Y, headers.indexOf("stop_lat"));

        //Create and add Nodes to network
        List<Node> nodes = createNodesFromData(dataLines, headersIndex);
        for(Node node:nodes)
            network.addNode(node);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads the route_sections file from the GTFS folder and creates links in the network based on the data.
     @param network the network to populate with links
     @param folderPath the path to the GTFS folder
     @throws RuntimeException if the route_sections file is missing required headers */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void readRouteSectionsFile(Network network, String folderPath)
    {
        //Extract File data
        String filePath = folderPath + ROUTE_SECTIONS_FILE;
        List<String[]> dataLines = this.extractData(filePath, ',');
        List<String> headers = Arrays.asList(dataLines.remove(0));

        //If file don't contain needed columns, error
        if(!headers.contains("from_stop_id") || !headers.contains("to_stop_id") || !headers.contains("traversal_time"))
            throw new RuntimeException("missing header in" + ROUTE_SECTIONS_FILE);

        //Associate Link attributes with data column index
        HashMap<LINK_ATTRIBUTES, Integer> headersIndex = new HashMap<>();
        headersIndex.put(LINK_ATTRIBUTES.FROM_ID, headers.indexOf("from_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.TO_ID, headers.indexOf("to_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.TIME, headers.indexOf("time"));

        //Create Links with file data
        List<Link> links = this.createLinksFromData(network, dataLines, headersIndex);

        //Add missing data and add to network (length, capacity, speed)
        for(Link link:links) {
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads the transfers file from the GTFS folder and creates links in the network based on the data.
     * Transfers = footpath between nearby stops of different modes of transport or services
     @param network the network to populate with links
     @param folderPath the path to the GTFS folder
     @throws RuntimeException if the route_sections file is missing required headers */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readTransfersFile(Network network, String folderPath)
    {
        //Extract File data
        String filePath = folderPath + TRANSFERS_FILE;
        List<String[]> dataLines = this.extractData(filePath, ',');
        List<String> headers = Arrays.asList(dataLines.remove(0));

        //If file don't contain needed columns, error
        if(!headers.contains("from_stop_id") || !headers.contains("to_stop_id") || !headers.contains("min_transfer_time"))
            throw new RuntimeException("missing header in" + TRANSFERS_FILE);

        //Associate Link attributes with data column index
        HashMap<LINK_ATTRIBUTES, Integer> headersIndex = new HashMap<>();
        headersIndex.put(LINK_ATTRIBUTES.FROM_ID, headers.indexOf("from_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.TO_ID, headers.indexOf("to_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.TIME, headers.indexOf("min_transfer_time"));

        //Create Links with file data
        List<Link> links = this.createLinksFromData(network, dataLines, headersIndex);

        //Add missing data and add to network (length, capacity, speed, bidirectional)
        for(Link link:links) {
            link.setNormalSpeedInKMH(2);
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads the pathways file from the GTFS folder and creates links in the network based on the data.
     * Pathway = specific paths inside the network (between two platforms of a metro station for example)
    @param network the network to populate with links
    @param folderPath the path to the GTFS folder
    @throws RuntimeException if the route_sections file is missing required headers */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readPathwayFile(Network network, String folderPath)
    {
        //Extract File data
        String filePath = folderPath + PATHWAYS_FILE;
        List<String[]> dataLines = this.extractData(filePath, ',');
        List<String> headers = Arrays.asList(dataLines.remove(0));

        //If file don't contain needed columns, error
        if(!headers.contains("pathway_id") || !headers.contains("from_stop_id") || !headers.contains("to_stop_id")
        || !headers.contains("is_bidirectional") || !headers.contains("length") || !headers.contains("traversal_time") )
            throw new RuntimeException("missing header in" + PATHWAYS_FILE);

        //Associate Link attributes with data column index
        HashMap<LINK_ATTRIBUTES, Integer> headersIndex = new HashMap<>();
        headersIndex.put(LINK_ATTRIBUTES.ID, headers.indexOf("pathway_id"));
        headersIndex.put(LINK_ATTRIBUTES.FROM_ID, headers.indexOf("from_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.TO_ID, headers.indexOf("to_stop_id"));
        headersIndex.put(LINK_ATTRIBUTES.BIDIRECTIONAL, headers.indexOf("is_bidirectional"));
        headersIndex.put(LINK_ATTRIBUTES.LENGTH, headers.indexOf("length"));
        headersIndex.put(LINK_ATTRIBUTES.TIME, headers.indexOf("traversal_time"));

        //Create Links with file data
        List<Link> links = this.createLinksFromData(network, dataLines, headersIndex);

        //Add missing data and add to network (capacity, bidirectional,speed)
        for(Link link:links) {
            link.setNormalSpeedInKMH(2);
            network.addLink(link);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Creates a stop links file from a stop times file
     * @param folderPath The path to the stop times file */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void readStopTimesFile(String folderPath)
    {
        List<String[]> trips = this.extractStopTimesLinksData(folderPath + STOP_TIMES_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + ROUTE_SECTIONS_FILE))) {
            writer.write("from_stop_id,to_stop_id,time");
            for (String[] stopTimeData : trips) {
                writer.newLine();
                writer.write(stopTimeData[0] + "," + stopTimeData[1] + "," + stopTimeData[2]);
            }
        }
        catch (IOException e) {e.printStackTrace();}
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Extracts stop times data from a given file and creates links between stops with associated delays
     * @param filePath The path to the stop times file.
     * @return A HashMap containing the stop links data, where the key is the link ID and the value is an array
     * containing the from_node_id, to_node_id, and delay */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private List<String[]> extractStopTimesLinksData(String filePath)
    {
        HashMap<String, String[]> linksData = new HashMap<>();
        List<String> dataLines = this.extractData(filePath);
        List<String> headers = Arrays.asList(dataLines.remove(0).split(","));
        for (int i = 0; i < dataLines.size() - 1; i++) {
            String[] firstDataLine = dataLines.get(i).split(",");
            String[] secondDataLine = dataLines.get(i + 1).split(",");
            //If two consecutive data lines belong to the same trip, create a link between stops
            if (firstDataLine[headers.indexOf("trip_id")].equals(secondDataLine[headers.indexOf("trip_id")])) {
                String from_node_id = firstDataLine[headers.indexOf("stop_id")];
                String to_node_id = secondDataLine[headers.indexOf("stop_id")];
                String link_id = from_node_id + ":" + to_node_id;
                String[] firstTimeStrings = firstDataLine[headers.indexOf("arrival_time")].split(":");
                String[] secondTimeStrings = secondDataLine[headers.indexOf("arrival_time")].split(":");
                int delay = this.getStopTimeDelay(firstTimeStrings,secondTimeStrings);
                //If the link already exists, keep the longest traversal time, otherwise add the link
                if (linksData.containsKey(link_id))
                    linksData.get(link_id)[2] = "" + Math.max(delay, Integer.parseInt(linksData.get(link_id)[2]));
                else
                    linksData.put(link_id, new String[]{from_node_id, to_node_id, "" + delay});
            }
        }
        return new ArrayList<>(linksData.values());
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Computes the delay between two given departure/arrival times.
     * @param firstTimeStrings  An array of strings representing the first time in the format HH:mm:ss
     * @param secondTimeStrings An array of strings representing the second time in the format HH:mm:ss
     * @return The delay in seconds between the two times */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private int getStopTimeDelay(String[] firstTimeStrings, String[] secondTimeStrings)
    {
        Duration firstTime = Duration.ofHours(Integer.parseInt(firstTimeStrings[0]))
                .plusMinutes(Integer.parseInt(firstTimeStrings[1]))
                .plusSeconds(Integer.parseInt(firstTimeStrings[2]));
        Duration secondTime = Duration.ofHours(Integer.parseInt(secondTimeStrings[0]))
                .plusMinutes(Integer.parseInt(secondTimeStrings[1]))
                .plusSeconds(Integer.parseInt(secondTimeStrings[2]));
        return (int) (secondTime.getSeconds() - firstTime.getSeconds());
    }
}