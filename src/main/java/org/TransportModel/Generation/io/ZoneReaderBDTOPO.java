package org.TransportModel.Generation.io;

import org.TransportModel.Generation.Area;
import org.TransportModel.Generation.Zone;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;

public class ZoneReaderBDTOPO
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** Imports a shapefile of BDTOPO format and creates links from the features
     * @param shpFilePath The path to the shapefile to import */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void readBDTOPOFile(Area area, String shpFilePath) throws IOException
    {
        File shapeFile = new File(shpFilePath);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL());
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        try(SimpleFeatureIterator featureIterator = featureSource.getFeatures().features()){
            while (featureIterator.hasNext())
                this.addFeatureZone(area, featureIterator.next());
        }
        catch(Exception e){e.printStackTrace();}
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /** */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private void addFeatureZone(Area area, SimpleFeature feature)
    {
        //Get values
        Object id = feature.getAttribute(ATTRIBUTES.ID);
        Object shapeLength = feature.getAttribute(ATTRIBUTES.SHAPE_LENGTH);
        Object department = feature.getAttribute(ATTRIBUTES.DEPARTMENT);
        //Convert
        if (id == null)
            return;
        MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
        Zone zone = new Zone(id.toString(),multiPolygon);
        area.addZone(zone);
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////
//Todo replace by config file
///////////////////////////////////////////////////////////////////////////////////////////////////
class ATTRIBUTES {
    public static final String ID = "objectid", SHAPE_LENGTH = "shape_leng", DEPARTMENT = "numdep";
}
