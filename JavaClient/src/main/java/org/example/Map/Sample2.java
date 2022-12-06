package org.example.Map;

import com.soap.ws.client.generated.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * A simple sample application that shows
 * a OSM map of Europe containing a route with waypoints
 * @author Martin Steiger
 */
public class Sample2
{
    JXMapViewer mapViewer = new JXMapViewer();
    static List<Itinary> checkItinary(String departure,String finish){
        System.out.println("---Welcome on Let's Biking app!---");
        Bike bike = new Bike();
        IBikeService bikeService = bike.getBasicHttpBindingIBikeService();
        List<Itinary> itinary = bikeService.getItinerary(departure,finish).getItinary();
        //Itinary itinary = bikeService.getItinerary("Livraison Par Le, 20 Rue de l'Amitié, Bd Président John Fitzgerald Kennedy, 25000 Besançon", "91-93 Bd Léon Blum, 25000 Besançon");
        //TODO (OU PAS DU COUP) MOURAD EFFACE PAS LES COMMENTAIRES COMME CA ON PEUT TESTER AVEC DES ADRESSES DIFFERENTES QUI ONT POSé PROBLEME
        //Itinary itinary = bikeService.getItinerary("Dieweg 69, 1180 Uccle, Belgique","Rue Geleytsbeek 2, 1180 Uccle, Belgique");
        //Itinary itinary = bikeService.getItinerary("87 Rue Greuze, 69100 Villeurbanne","21 Rue Flachet, 69100 Villeurbanne");
        if(itinary == null){
            System.out.println("UNE ERREUR EST SURVENUE");
            return null;
        }
        return itinary;
    }

    /**
     * @param args the program args (ignored)
     */
    public static void main(String[] args)
    {
        Sample2 sample2=new Sample2();

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("JXMapviewer2 Example 2");
        UserGUI userGUI=new UserGUI("test",sample2);
        frame.add(userGUI.getMyPanel());
        frame.setLayout(new GridLayout());
        frame.add(sample2.mapViewer);
        sample2.mapViewer.setSize(800,600);
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(sample2.mapViewer);
        sample2.mapViewer.addMouseListener(mia);
        sample2.mapViewer.addMouseMotionListener(mia);

        sample2.mapViewer.addMouseListener(new CenterMapListener(sample2.mapViewer));

        sample2.mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(sample2.mapViewer));

        sample2.mapViewer.addKeyListener(new PanKeyListener(sample2.mapViewer));

        // Add a selection painter
        SelectionAdapter sa = new SelectionAdapter(sample2.mapViewer);
        SelectionPainter sp = new SelectionPainter(sa);
        sample2.mapViewer.addMouseListener(sa);
        sample2.mapViewer.addMouseMotionListener(sa);
        sample2.mapViewer.setOverlayPainter(sp);

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        sample2.mapViewer.setTileFactory(tileFactory);
    }

    public List<Itinary> generateMap(String departure, String arrival){
        //Livraison Par Le, 20 Rue de l'Amitié, Bd Président John Fitzgerald Kennedy, 25000 Besançon
        // 91-93 Bd Léon Blum, 25000 Besançon

        List<GeoPosition> trackOnFoot=new ArrayList<>();
        List<GeoPosition> trackOnBycicle=new ArrayList<>();

        Set<Waypoint> waypoints=new HashSet<>();

        List<Itinary> itinaryList=checkItinary(departure,arrival);
        for (Itinary itinary:itinaryList){
            for(FeatureItinary feature : itinary.getFeatures().getValue().getFeatureItinary()){
                Boolean first=true;
                for(ArrayOfdouble doubles :feature.getGeometry().getValue().getCoordinates().getValue().getArrayOfdouble()) {
                    List<Double> doubleList = doubles.getDouble();
                    if (first) waypoints.add(new DefaultWaypoint(doubleList.get(1), doubleList.get(0)));
                    if (itinary.isOnFoot()) trackOnFoot.add(new GeoPosition(doubleList.get(1), doubleList.get(0)));
                    else trackOnBycicle.add(new GeoPosition(doubleList.get(1), doubleList.get(0)));
                    first = false;
                }
            }
        }
        waypoints.add(new DefaultWaypoint(trackOnFoot.get(trackOnFoot.size()-1).getLatitude(),trackOnFoot.get(trackOnFoot.size()-1).getLongitude()));
        RoutePainter routePainterBycicle = new RoutePainter(trackOnFoot,Color.GREEN);
        RoutePainter routePainterFoot = new RoutePainter(trackOnFoot,Color.BLUE);

        List<GeoPosition>  track=new ArrayList<>();
        track.addAll(trackOnFoot);
        track.addAll(trackOnBycicle);

        // Set the focus
        mapViewer.zoomToBestFit(new HashSet<GeoPosition>(List.of(track.get(0))), 0.7);


        // Create a waypoint painter that takes all the waypoints
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();


        waypointPainter.setWaypoints(waypoints);

        // Create a compound painter that uses both the route-painter and the waypoint-painter
        List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
        painters.add(routePainterFoot);
        painters.add(waypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);

        mapViewer.setOverlayPainter(painter);
        return(itinaryList);
    }



}