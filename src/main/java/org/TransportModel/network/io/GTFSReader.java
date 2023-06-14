package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;
import org.locationtech.jts.geom.Coordinate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

///////////////////////////////////////////////////////////////////////////////////////////////////
/** GTFSReader is a class that reads GTFS (General Transit Feed Specification) to fill a network */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class GTFSReader
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads a GTFS folder and fill the network with data
     * @param network the network to fill
     * @param folderPath the path to the GTFS folder
     * @throws FileNotFoundException if a required GTFS file is not found */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readGTFSFolder(Network network, String folderPath) throws IOException
    {
        //If Custom route_sections file don't exist, create it
        if(!new File(folderPath + GTFS_FILES.ROUTE_SECTIONS).exists())
            this.createRouteSectionsFile(folderPath);
        //If Optional pathways file don't exist, toDo create it
        if(!new File(folderPath + GTFS_FILES.PATHWAYS).exists())
            return;
        //If Optional transfers file don't exist, toDo create it
        if (new File(folderPath + GTFS_FILES.TRANSFERS).exists())
            return;
        this.readStopFile(network, folderPath);
        this.readRouteSectionsFile(network,folderPath);
        this.readPathwayFile(network, folderPath);
        this.readTransfersFile(network, folderPath);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each stop, create a node object and add it to the network
     * @param network the network to populate with nodes
     * @param folderPath the path to the GTFS folder */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void readStopFile(Network network, String folderPath) throws IOException
    {
        Path filePath = Paths.get(folderPath, GTFS_FILES.STOPS);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String stop_id = values[headers.indexOf(STOPS.ID)];//If header don't exist: error
            String lon_string = values[headers.indexOf(STOPS.LON)];
            String lat_string = values[headers.indexOf(STOPS.LAT)];
            double lon = Double.parseDouble(lon_string);//If wrong format or empty data:error
            double lat = Double.parseDouble(lat_string);
            Coordinate coordinate = new Coordinate(lon,lat);
            Node node = new Node(stop_id, coordinate);
            network.addNode(node);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each route section, create a link object and add it to the network
     * Route section = fragment of a route connecting two stops
     * @param network the network to populate with links
     * @param folderPath the path to the GTFS folder */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void readRouteSectionsFile(Network network, String folderPath) throws IOException
    {
        Path filePath = Paths.get(folderPath, GTFS_FILES.ROUTE_SECTIONS);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String route_id = values[headers.indexOf(SECTIONS.ROUTE_ID)];//If header don't exist: error
            String type_string = values[headers.indexOf(SECTIONS.ROUTE_TYPE)];
            String from_id = values[headers.indexOf(SECTIONS.FROM_ID)];
            String to_id = values[headers.indexOf(SECTIONS.TO_ID)];
            String time_string = values[headers.indexOf(SECTIONS.TIME)];
            String frequency_string = values[headers.indexOf(SECTIONS.FREQUENCY)];
            double timeInS = Double.parseDouble(time_string);//If wrong format or empty data:error
            double frequencyInS = Double.parseDouble(frequency_string);
            int route_type = Integer.parseInt(type_string);
            if(!network.containsNode(from_id)||!network.containsNode(to_id))//If NodeId not found in the network:error
                throw new RuntimeException("Stop not found: "+dataLine);
            Node fromNode = network.getNode(from_id);
            Node toNode = network.getNode(to_id);
            String section_id = route_id+":"+from_id+":"+to_id;
            double lengthInM = fromNode.getDistanceInM(toNode);
            double speedInMS = lengthInM / timeInS;
            int maxCapacity = this.getMaxCapacity(route_type);
            double capacityPerHour = maxCapacity / frequencyInS / 3600;
            Link link = new Link(section_id, fromNode, toNode, false, speedInMS, capacityPerHour, lengthInM);
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each transfer, create a link object and add it to the network
     * Transfers = footpath between nearby stops of different modes of transport
     * @param network the network to populate with links
     * @param folderPath the path to the GTFS folder */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readTransfersFile(Network network, String folderPath) throws IOException
    {
        Path filePath = Paths.get(folderPath, GTFS_FILES.TRANSFERS);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String from_id = values[headers.indexOf(TRANSFERS.FROM_ID)];//If header don't exist: error
            String to_id = values[headers.indexOf(TRANSFERS.TO_ID)];
            String time_string = values[headers.indexOf(TRANSFERS.TIME)];
            double timeInS = Double.parseDouble(time_string);//If wrong format or empty data:error
            String id = from_id+':'+to_id;
            if(!network.containsNode(from_id)||!network.containsNode(to_id))//If NodeId not found in the network:error
                throw new RuntimeException("Stop not found: "+dataLine);
            Node fromNode = network.getNode(from_id);
            Node toNode = network.getNode(to_id);
            double lengthInM = fromNode.getDistanceInM(toNode);
            double speedInMS = lengthInM / timeInS;
            double capacityPerHour = 10000000;
            Link link = new Link(id, fromNode, toNode, true, speedInMS, capacityPerHour, lengthInM);
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each pathway, create a link object and add it to the network
     * Pathway = specific paths inside the network (between two platforms of a metro station for example)
     * @param network the network to populate with links
     * @param folderPath the path to the GTFS folder */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readPathwayFile(Network network, String folderPath) throws IOException
    {
        Path filePath = Paths.get(folderPath, GTFS_FILES.PATHWAYS);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String id = values[headers.indexOf(PATHWAYS.ID)];//If header don't exist: error
            String from_id = values[headers.indexOf(PATHWAYS.FROM_ID)];
            String to_id = values[headers.indexOf(PATHWAYS.TO_ID)];
            String time_string = values[headers.indexOf(PATHWAYS.TIME)];
            String length_string = values[headers.indexOf(PATHWAYS.LENGTH)];
            String bidirectional_string = values[headers.indexOf(PATHWAYS.BIDIRECTIONAL)];
            double timeInS = Double.parseDouble(time_string);//If wrong format or empty data:error
            if(!network.containsNode(from_id)||!network.containsNode(to_id))//If NodeId not found in the network:error
                throw new RuntimeException("Stop not found: "+dataLine);
            Node fromNode = network.getNode(from_id);
            Node toNode = network.getNode(to_id);
            double lengthInM = Double.parseDouble(length_string);
            double speedInMS = lengthInM / timeInS;
            double capacityPerHour = 10000000;
            boolean bidirectional =  bidirectional_string.equals("1");
            Link link = new Link(id, fromNode, toNode, bidirectional, speedInMS, capacityPerHour, lengthInM);
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns the maximum capacity for a given route type
     @param route_type The route type identifier
     @return The maximum capacity value corresponding to the given route type */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private int getMaxCapacity(int route_type)
    {
        switch(route_type) {
            case 0:return 1000;//Tram or light subway
            case 1:return 1001;//Subway
            case 2:return 1002;//Train
            case 3:return 1003;//Bus
            case 5:return 1005;//Tram
            default:return 1006;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Create the custom route sections file to the specified folder path with trips, routes and stop_times data
     * Route section = fragment of a route connecting two stops
     * It contains for each route section: route_id,route_type,from_stop_id,to_stop_id,time,frequency
     * The frequency correspond to the average frequency of passages of every trip of the route
     * The time correspond to the average travel time between the two section points of every trip of the route
     * @param folderPath the path to the GTFS folder */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void createRouteSectionsFile(String folderPath) throws IOException
    {
        //Extract data from existing files
        HashMap<String,HashMap<String,Integer>> tripStops = this.readStopTimesFile(folderPath);//<TripId,<StopIds,Times>>
        HashMap<String,List<String>> routeTrips = this.readTripsFile(folderPath);//<RouteId,TripIds>
        HashMap<String,String> routesTypes = this.readRoutesFile(folderPath);//<RouteId,RouteType>
        //Fus data
        List<HashMap<String,String>> lines = new ArrayList<>();
        for(Map.Entry<String, String> routes:routesTypes.entrySet())
        {
            String routeId = routes.getKey();
            String routeType = routes.getValue();
            List<String> tripsIds = routeTrips.get(routeId);
            int routeFrequency = this.getAverageFrequency(tripsIds,tripStops);
            HashMap<String,String> sections = this.getSections(tripStops.get(tripsIds.get(0)));
            for(Map.Entry<String, String> stopsId:sections.entrySet())
            {
                String fromId = stopsId.getKey();
                String toId = stopsId.getValue();
                int traversalTime = this.getAverageTime(tripsIds,tripStops,fromId,toId);
                HashMap<String,String> line = new HashMap<>();
                line.put(SECTIONS.ROUTE_ID,routeId);
                line.put(SECTIONS.ROUTE_TYPE,routeType);
                line.put(SECTIONS.FREQUENCY,""+routeFrequency);
                line.put(SECTIONS.FROM_ID,fromId);
                line.put(SECTIONS.TO_ID,toId);
                line.put(SECTIONS.TIME,""+traversalTime);
                lines.add(line);
            }
        }
        //Write route sections file
        this.writeRouteSectionsFile(folderPath,lines);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Writes the custom route sections file to the specified folder path
     * Route section = fragment of a route connecting two stops
     * @param folderPath the path to the GTFS folder
     * @param lines A list of hashMap<Header,Value> representing each line of the file */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void writeRouteSectionsFile(String folderPath, List<HashMap<String,String>> lines) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + GTFS_FILES.ROUTE_SECTIONS));
        writer.write(String.join(",", SECTIONS.ROUTE_ID, SECTIONS.ROUTE_TYPE,
                SECTIONS.FROM_ID, SECTIONS.TO_ID, SECTIONS.TIME, SECTIONS.FREQUENCY));
        writer.newLine();
        for(HashMap<String,String> line: lines)
        {
            writer.write(String.join(",", line.get(SECTIONS.ROUTE_ID),line.get(SECTIONS.ROUTE_TYPE),
                    line.get(SECTIONS.FROM_ID), line.get( SECTIONS.TO_ID), line.get(SECTIONS.TIME), line.get(SECTIONS.FREQUENCY)));
            writer.newLine();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each trip, get all the stops and their associated arrival time
     * @param folderPath the path to the GTFS folder
     * @return a trip-stops HashMap <tripId,<stopId,arrivalTime>> */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private HashMap<String,HashMap<String,Integer>> readStopTimesFile(String folderPath) throws IOException
    {
        HashMap<String,HashMap<String,Integer>> tripStops = new HashMap<>();
        Path filePath = Paths.get(folderPath, GTFS_FILES.STOP_TIMES);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String stop_id = values[headers.indexOf(TIMES.STOP_ID)];//If header don't exist: error
            String time_string = values[headers.indexOf(TIMES.ARRIVAL_TIME)];
            String trip_id = values[headers.indexOf(TIMES.TRIP_ID)];
            String[] time_strings = time_string.split(":");
            int hours = Integer.parseInt(time_strings[0]);//If wrong format or empty data:error
            int minutes = Integer.parseInt(time_strings[1]);
            int seconds = Integer.parseInt(time_strings[2]);
            int arrivalTimeInS = (hours * 3600) + (minutes * 60) + seconds;
            if(!tripStops.containsKey(trip_id))
                tripStops.put(trip_id, new HashMap<>());
            tripStops.get(trip_id).put(stop_id,arrivalTimeInS);
        }
        return tripStops;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each routeId, get all the tripsIds
     * @param folderPath the path to the GTFS folder
     * @return A route-trips HashMap <routeId,tripsIds> */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private HashMap<String,List<String>> readTripsFile(String folderPath) throws IOException
    {
        HashMap<String,List<String>> routeTrips = new HashMap<>();
        Path filePath = Paths.get(folderPath, GTFS_FILES.TRIPS);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String trip_id = values[headers.indexOf(TRIPS.ID)];//If header don't exist: error
            String route_id = values[headers.indexOf(TRIPS.ROUTE_ID)];
            if(!routeTrips.containsKey(route_id))
                routeTrips.put(route_id,new ArrayList<>());
            routeTrips.get(route_id).add(trip_id);
        }
        return routeTrips;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** For each route, get the route type (0:light tram/metro, 1:metro, 2:train, 3:bus)
     * @param folderPath the path to the GTFS folder
     * @return A route-type HashMap <routeId,routeType> */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private HashMap<String, String>  readRoutesFile(String folderPath) throws IOException
    {
        HashMap<String, String> routesTypes = new HashMap<>();
        Path filePath = Paths.get(folderPath, GTFS_FILES.ROUTES);
        List<String> lines = Files.readAllLines(filePath);//If file don't exist:error
        List<String> headers = Arrays.asList(lines.remove(0).split(","));
        for (String dataLine : lines)
        {
            String[] values = dataLine.split(",");
            if(values.length != headers.size())//If delimiter in data: error
                throw new RuntimeException("Delimiter in data: "+dataLine);
            String route_id = values[headers.indexOf(ROUTES.ID)];//If header don't exist: error
            String route_type = values[headers.indexOf(ROUTES.TYPE)];
            routesTypes.put(route_id,route_type);
        }
        return routesTypes;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Calculates the average departure frequency of the trips in parameter
     * @param tripsIds A list of trip IDs for which to calculate the average departure frequency
     * @param tripStops a trip-stops HashMap <tripId,<stopId,arrivalTime>>
     * @return the average departure frequency in seconds */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public int getAverageFrequency(List<String> tripsIds, HashMap<String,HashMap<String,Integer>> tripStops)
    {
        List<Integer> departures = new ArrayList<>();
        for(String tripId:tripsIds)
            departures.add(Collections.min(tripStops.get(tripId).values()));
        //Compute frequency
        int firstDeparture = Collections.min(departures);
        int lastDeparture = Collections.max(departures);
        int passagesNumbers = tripsIds.size();
        return (lastDeparture-firstDeparture)/passagesNumbers;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns a HashMap containing sections (= two consecutive stops) based on the arrival times
     * @param stopsArrivalTimes A stopTimes HashMap <stopId,arrivalTime>
     * @return the sections HashMap <fromId,toId>*/
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private HashMap<String,String> getSections(HashMap<String,Integer> stopsArrivalTimes)
    {
        HashMap<String,String> sections = new HashMap<>();
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(stopsArrivalTimes.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        for (int stopSequence = 0; stopSequence < entries.size()-1; stopSequence++)
        {
            String fromId = entries.get(stopSequence).getKey();
            String toId = entries.get(stopSequence + 1).getKey();
            sections.put(fromId,toId);
        }
        return sections;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Returns the average traversal time between two stops for a list of trips
     * @param tripsIds A list of trip IDs for which to calculate the average traversal time
     * @param tripStops a trip-stops HashMap <tripId,<stopId,arrivalTime>>
     * @param fromId The ID of the starting stop
     * @param toId The ID of the destination stop
     * @return The average traversal time in minutes between the specified stops for the given trips */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private int getAverageTime(List<String> tripsIds, HashMap<String,HashMap<String,Integer>> tripStops, String fromId, String toId)
    {
        int totalTraversalTimes = 0;
        int passageNumbers = 0;
        for(String tripId:tripsIds) {
            HashMap<String,Integer> stopsTimes = tripStops.get(tripId);
            int fromTime = stopsTimes.get(fromId);
            int toTime = stopsTimes.get(toId);
            int traversalTime = toTime - fromTime;
            totalTraversalTimes+=traversalTime;
            passageNumbers++;
        }
        return totalTraversalTimes/passageNumbers;
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////
//Todo replace by config file
///////////////////////////////////////////////////////////////////////////////////////////////////
class GTFS_FILES {
    public static final String STOP_TIMES = "/stop_times.txt",STOPS="/stops.txt",ROUTE_SECTIONS="/route_sections.txt",
            PATHWAYS = "/pathways.txt",TRANSFERS = "/transfers.txt",TRIPS = "/trips.txt", ROUTES = "/routes.txt";
}
class STOPS {
    public static final String ID = "stop_id", LON = "stop_lon",LAT = "stop_lat";
}
class SECTIONS {
    public static final String ROUTE_ID = "route_id",ROUTE_TYPE = "route_type", FROM_ID = "from_stop_id",
            TO_ID = "to_stop_id", TIME = "time",FREQUENCY = "frequency";
}
class TRANSFERS {
    public static final String FROM_ID = "from_stop_id", TO_ID = "to_stop_id", TIME = "min_transfer_time";
}
class PATHWAYS {
    public static final String ID = "pathway_id",FROM_ID = "from_stop_id",TO_ID = "to_stop_id",
            BIDIRECTIONAL = "is_bidirectional", LENGTH = "length",TIME = "traversal_time";
}
class TRIPS {
    public static final String ROUTE_ID = "route_id", ID = "trip_id";
}
class ROUTES {
    public static final String ID = "route_id",TYPE = "route_type";
}
class TIMES {
    public static final String TRIP_ID = "trip_id",ARRIVAL_TIME = "arrival_time",STOP_ID = "stop_id";
}