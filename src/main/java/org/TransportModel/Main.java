package org.TransportModel;

import org.TransportModel.GUI.NetworkCanvas;
import org.TransportModel.GUI.UserInterface;
import org.TransportModel.Generation.Area;
import org.TransportModel.Generation.io.ZoneReaderBDTOPO;
import org.TransportModel.network.Network;
import org.TransportModel.network.io.NetworkReaderBDTOPO;
import org.TransportModel.network.io.NetworkReaderGTFS;
///////////////////////////////////////////////////////////////////////////////////////////////////
/**                                       Main Class                                             */
///////////////////////////////////////////////////////////////////////////////////////////////////
public class Main
{
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**                                        Main Function                                         */
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String[] args)
    {
        //TI
        Network network_TI = new Network();
        NetworkReaderBDTOPO networkReaderBDTOPO = new NetworkReaderBDTOPO();
        String shpFilePath = "src/main/resources/TI/BDTOPO_94/TRONCON_DE_ROUTE.shp";
        try{networkReaderBDTOPO.readBDTOPOFile(network_TI,shpFilePath);}
        catch(Exception e){e.printStackTrace();}

        //TC
        Network network_TC = new Network();
        NetworkReaderGTFS networkReaderGTFS = new NetworkReaderGTFS();
        String gtfsFolderPath = "src/main/resources/TC/GTFS_IDF";
        try{networkReaderGTFS.readGTFSFolder(network_TC,gtfsFolderPath);}
        catch(Exception e){e.printStackTrace();}

        //Display Network
        UserInterface gUI = new UserInterface();
        NetworkCanvas networkCanvas = new NetworkCanvas(network_TC);
        gUI.setComponent(networkCanvas);


        //Zone
        Area idf = new Area();
        ZoneReaderBDTOPO zoneReaderBDTOPO = new ZoneReaderBDTOPO();
        String communesShapeFile = "src/main/resources/Zone/BDTOPO_IDF/communes.shp";
        try{zoneReaderBDTOPO.readBDTOPOFile(idf, communesShapeFile);}
        catch(Exception e){e.printStackTrace();}


    }
}