package org.TransportModel;

import org.TransportModel.GUI.NetworkCanvas;
import org.TransportModel.GUI.UserInterface;
import org.TransportModel.network.Network;
import org.TransportModel.network.io.GTFSReader;
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
        /*
        //TI
        Network network_TI = new Network();
        BDTOPOReader bdtopoReader = new BDTOPOReader();
        String shpFilePath = "src/main/resources/TI/TRANSPORT-94/TRONCON_DE_ROUTE.shp";
        bdtopoReader.readBDTOPOFile(network_TI,shpFilePath);
        */

        //TC
        Network network_TC = new Network();
        GTFSReader gtfsReader = new GTFSReader();
        String gtfsFolderPath = "src/main/resources/TC/GTFS_IDF";

        try{gtfsReader.readGTFSFolder(network_TC,gtfsFolderPath);}
        catch(Exception e){e.printStackTrace();}

        //Display Network
        UserInterface gUI = new UserInterface();
        NetworkCanvas networkCanvas = new NetworkCanvas(network_TC);
        gUI.setComponent(networkCanvas);
    }
}