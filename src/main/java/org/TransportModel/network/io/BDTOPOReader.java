package org.TransportModel.network.io;

import org.TransportModel.network.Link;
import org.TransportModel.network.Network;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.util.List;

///////////////////////////////////////////////////////////////////////////////////////////////////
/** BDTOPOReader is a class that reads BDTOPO Files and fill a network */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class BDTOPOReader extends NetworkReader
{
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
            if(isFeatureValid(feature)) {
                List<Link> links = this.createLinksFromMultiLineString((MultiLineString)feature.getDefaultGeometry());
                this.setLinksAttributes(links,feature);
                for(Link link:links)
                    network.addLink(link);
            }
        }
        featureIterator.close();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Sets attributes (capacity, speed, direction) for a list of links of a given feature
     * @param links The list of links to set attributes for
     * @param feature The feature containing the attribute values */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void setLinksAttributes(List<Link> links, SimpleFeature feature)
    {
        for(Link link:links) {
            link.setNormalSpeedInKMH((Integer) feature.getAttribute(SPEED));
            link.setBidirectional(feature.getAttribute(DIRECTION).equals(DIRECTION_BIDIRECTIONAL));
            try {
                int lanes_Nbr = (Integer) feature.getAttribute(LANES_NBR);
                link.setCapacityPerHour(lanes_Nbr * 1800);
            }
            catch(Exception e){link.setCapacityPerHour(1800);}
            if(feature.getAttribute(DIRECTION).equals(DIRECTION_INVERSE))
                link.inverseDirection();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Checks if a given feature is valid
     * @param feature The feature to check
     * @return {@code true} if the feature is valid, {@code false} otherwise */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean isFeatureValid(SimpleFeature feature)
    {
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        boolean isMultiLineString = geometry instanceof MultiLineString;
        boolean isAccessibleToVL = feature.getAttribute(ACCESS).equals(ACCESS_FREE);
        return isMultiLineString && isAccessibleToVL;
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
