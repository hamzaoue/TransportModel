package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;

///////////////////////////////////////////////////////////////////////////////////////////////////
/** BDTOPOReader is a class that reads BDTOPO Files and fill a network */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class NetworkReaderBDTOPO
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Imports a shapefile of BDTOPO format and creates links from the features
     * @param shpFilePath The path to the shapefile to import
     * @param network The network to add the created links to */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readBDTOPOFile(Network network, String shpFilePath) throws IOException
    {
        File shapeFile = new File(shpFilePath);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        try(SimpleFeatureIterator featureIterator = featureSource.getFeatures().features()){
            while (featureIterator.hasNext())
                this.addFeatureLinks(network, featureIterator.next());
        }
        catch(Exception e){e.printStackTrace();}
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Create and adds feature links to the network
     * @param network The network to which the feature links will be added
     * @param feature The SimpleFeature containing the information for creating the links */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void addFeatureLinks(Network network, SimpleFeature feature) throws FactoryException, TransformException
    {
        //Get values
        Object access = feature.getAttribute(ATTRIBUTES.ACCESS);
        Object lanes_nbr = feature.getAttribute(ATTRIBUTES.LANES_NBR);
        Object speed = feature.getAttribute(ATTRIBUTES.SPEED);
        Object direction = feature.getAttribute(ATTRIBUTES.DIRECTION);
        //Convert
        if (!access.equals(VALUES.FREE) || lanes_nbr == null || speed == null || direction == null)
            return;
        boolean bidirectional = direction.equals(VALUES.BIDIRECTIONAL);
        double speedInKMH = (Integer) feature.getAttribute(ATTRIBUTES.SPEED);
        double speedInMS = speedInKMH * (1000.0 / 3600.0);
        int lanes_Nbr = (Integer) feature.getAttribute(ATTRIBUTES.LANES_NBR);
        double maxCapacity = lanes_Nbr * 1800;
        //Create nodes and links
        MultiLineString multiLineString = (MultiLineString) feature.getDefaultGeometry();
        for (int lineStringIndex = 0; lineStringIndex < multiLineString.getNumGeometries(); lineStringIndex++)
        {
            LineString lineString = (LineString) multiLineString.getGeometryN(lineStringIndex);
            Coordinate[] lambertCoords = lineString.getCoordinates();
            Coordinate[] coords = new Coordinate[lambertCoords.length];
            for(int i = 0; i < lambertCoords.length;i++)
                coords[i]=this.convertLambert93ToDegrees(lambertCoords[i]);
            //From
            Coordinate fromCoordinate = (direction.equals(VALUES.INVERSE)) ? coords[coords.length - 1] : coords[0];
            String fromNodeId = fromCoordinate.getX() + ":" + fromCoordinate.getY();
            Node fromNode = new Node(fromNodeId, fromCoordinate);
            //To
            Coordinate toCoordinate = (direction.equals(VALUES.INVERSE)) ? coords[0] : coords[coords.length - 1];
            String toNodeId = toCoordinate.getX() + ":" + toCoordinate.getY();
            Node toNode = new Node(toNodeId, toCoordinate);
            //Link
            String LinkId = fromNode.getId() + ":" + toNode.getId();
            double totalLength = 0;
            for(int coordinateIndex = 0; coordinateIndex <coords.length-1;coordinateIndex++)
               totalLength += this.calculateDistance(coords[coordinateIndex],coords[coordinateIndex+1]);
            Link link = new Link(LinkId, fromNode, toNode, bidirectional, speedInMS, maxCapacity, totalLength);
            network.addLink(link);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Converts Lambert-93 coordinates to degrees (latitude and longitude)
     * @param lambertCoordinate The Lambert-93 coordinate to be converted
     * @return A Coordinate object representing the converted latitude and longitude in degrees */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public Coordinate convertLambert93ToDegrees(Coordinate lambertCoordinate) throws FactoryException, TransformException
    {
        //Lambert-93
        CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:2154");
        //WGS84
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
        MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        DirectPosition2D sourcePosition = new DirectPosition2D(sourceCRS, lambertCoordinate.getX(), lambertCoordinate.getY());
        DirectPosition2D targetPosition = new DirectPosition2D();
        transform.transform(sourcePosition, targetPosition);
        double latitude = targetPosition.getY();
        double longitude = targetPosition.getX();
        return new Coordinate(latitude, longitude);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Calculates the distance in meters between two coordinates
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     * @return The distance in meters between the two coordinates */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public double calculateDistance(Coordinate coordinate1, Coordinate coordinate2)
    {
        GeodeticCalculator calculator = new GeodeticCalculator();
        calculator.setStartingGeographicPoint(coordinate1.getX(), coordinate1.getY());
        calculator.setDestinationGeographicPoint(coordinate2.getX(), coordinate2.getY());
        return calculator.getOrthodromicDistance();
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////
//Todo replace by config file
///////////////////////////////////////////////////////////////////////////////////////////////////
class ATTRIBUTES{
    public static final String SPEED = "VIT_MOY_VL",ACCESS = "ACCES_VL", DIRECTION = "SENS",LANES_NBR = "NB_VOIES";
}
class VALUES{
    public final static String FREE = "Libre", INVERSE = "Sens inverse",
            BIDIRECTIONAL = "Double sens";
}