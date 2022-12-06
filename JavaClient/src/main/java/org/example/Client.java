package org.example;

import POJO.ItinaryJava;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soap.ws.client.generated.*;
import org.example.Map.UserGUI;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Client {
    Bike bike;
    IBikeService bikeService;
    Consumer consumer;
    public Client(UserGUI userGui) throws JMSException,NamingException {
        this.bike = new Bike();
        this.bikeService = bike.getBasicHttpBindingIBikeService();
        consumer = new Consumer(userGui);

    }

    public ArrayOfItinary getItinary() throws Exception {
        ArrayOfItinary itinary = bikeService.getItinerary("Livraison Par Le, 20 Rue de l'Amitié, Bd Président John Fitzgerald Kennedy, 25000 Besançon", "91-93 Bd Léon Blum, 25000 Besançon", "");
        if(itinary == null) throw new Exception("Nous n'avons pas pu répondre à votre demande.");
        return itinary;
    }

    public void getItinaryByQueue() throws Exception {
        bikeService.putDataContainerInQueue("Livraison Par Le, 20 Rue de l'Amitié, Bd Président John Fitzgerald Kennedy, 25000 Besançon", "91-93 Bd Léon Blum, 25000 Besançon", "");
    }
    public String readQueue() throws Exception {
        String json = consumer.receiveMessage2();
        ActiveMqResponse response =  new ObjectMapper().readValue(json, ActiveMqResponse.class);
        if(response.exception == null)
            return response.toString();
        else
            return response.exception;
    }

    public void update() throws Exception {
        bikeService.update();}
    public void getItinaryByQueue(String startPosText, String finishPosText) throws Exception {
        bikeService.putDataContainerInQueue(startPosText, finishPosText);
    }


}
