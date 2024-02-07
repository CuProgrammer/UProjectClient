package com.asteral.clientdemo;

import java.io.File;
import java.util.ArrayList;

public class ClientDemo {

    public static void main(String[] args) {
        setup();
        RequestManager manager = new RequestManager("localhost", 8080);
        System.out.println(manager.login("Ermia", "kei"));
        manager.downloadProductFile(new Product("F"), "Image");
        
        ArrayList<Product> results = manager.getSellerProducts("Ermia");
       for (Product result:results)
            System.out.println(result);
    }
    
    public static void setup() {
        File images = new File("Images");
        File videos = new File("Videos");
        File music = new File("Music");
        
        if (!images.isDirectory())
            images.mkdir();
        if (!videos.isDirectory())
            videos.mkdir();
        if (!music.isDirectory())
            music.mkdir();
    }
}
