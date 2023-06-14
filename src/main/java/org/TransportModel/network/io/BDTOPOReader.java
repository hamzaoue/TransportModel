package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.TransportModel.network.Node;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;

///////////////////////////////////////////////////////////////////////////////////////////////////
/** BDTOPOReader is a class that reads BDTOPO Files and fill a network */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class BDTOPOReader {
    public final static String SPEED = "VIT_MOY_VL";
    public final static String ACCESS = "ACCES_VL";
    public final static String DIRECTION = "SENS";
    public final static String LANES_NBR = "NB_VOIES";
    public final static String ACCESS_FREE = "Libre";
    public final static String DIRECTION_INVERSE = "Sens inverse";
    public final static String DIRECTION_BIDIRECTIONAL = "Double sens";
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Imports a shapefile of BDTOPO format and creates links from the features
     * @param shpFilePath The path to the shapefile to import
     * @param network The network to add the created links to */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readBDTOPOFile(Network network, String shpFilePath)
    {
        SimpleFeatureIterator featureIterator = this.createFeatureIterator(shpFilePath);
        while (featureIterator.hasNext())
        {
            SimpleFeature feature = featureIterator.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if(geometry instanceof MultiLineString && feature.getAttribute(ACCESS).equals(ACCESS_FREE))
            {
                MultiLineString multiLineString = (MultiLineString)feature.getDefaultGeometry();
                int numGeometries = multiLineString.getNumGeometries();
                for (int i = 0; i < numGeometries; i++)
                {
                    LineString lineString = (LineString) multiLineString.getGeometryN(i);
                    Coordinate[] lineCoordinates = lineString.getCoordinates();
                    Coordinate from = lineCoordinates[0];
                    Coordinate to = lineCoordinates[lineCoordinates.length-1];
                    Node fromNode = new Node( from.x + ":" + from.y, from);
                    Node toNode = new Node(to.x + ":" + to.y, to);
                    String id = fromNode.getId() + ":" + toNode.getId();
                    double speedInKMH = (Integer) feature.getAttribute(SPEED);
                    double speedInMS = speedInKMH * (1000.0 / 3600.0);
                    boolean bidirectional = feature.getAttribute(DIRECTION).equals(DIRECTION_BIDIRECTIONAL);
                    int lanes_Nbr = (Integer) feature.getAttribute(LANES_NBR);
                    double maxCapacity = lanes_Nbr * 1800;
                    double length = lineString.getLength();
                    Link link = new Link(id,fromNode,toNode,bidirectional,speedInMS,maxCapacity,length);
                    if(feature.getAttribute(DIRECTION).equals(DIRECTION_INVERSE))
                        link.inverseDirection();
                    network.addLink(link);
                }
            }
        }
        featureIterator.close();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Reads a shapefile and returns a FeatureIterator representing the features in the shapefile
     * @param shpFilePath The path to the shapefile to be read
     * @return A FeatureIterator representing the features in the shapefile
     * @throws RuntimeException if any error occurs during the reading process */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private SimpleFeatureIterator createFeatureIterator(String shpFilePath)
    {
        try {
            File shapeFile = new File(shpFilePath);
            ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
            return featureSource.getFeatures().features();
        }
        catch (Exception e) {throw new RuntimeException(e);}
    }
}
