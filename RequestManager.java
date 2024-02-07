package com.asteral.clientdemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RequestManager {
    private String hostName;
    private int portNumber;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ShopUser user;

    public RequestManager(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }
    
    public ShopUser getUserInfo() {
        return user;
    }
    
    public void downloadProductFile(Product product, String type) {
        connect();
        tell("DownloadProductFile", product.getName(), type);
        downloadFile(product.getFilePath(type));
        disconnect();
    }
    
    public boolean uploadProductFile(Product product, String path, String type) {
        connect();
        tell("UploadProductFile", user, product.getName(), type);
        String response = listen();
        System.out.println(response);
        if (!response.equals("UploadProductFile Successful")) {
            disconnect();
            return false;
        }
        uploadFile(path);
        disconnect();
        
        return true;
    }
    
    public String generateGiftcard(double value) {
        connect();
        tell("GenerateGiftcard", user, value);
        String response = listen();
        System.out.println(response);
        disconnect();
        if (response.split("\t")[0].equals("GenerateGiftcard Successful"))
            return response.split("\t")[1];
        else
            return "Unexpected Error";
    }
    
    public ArrayList<Product> search(String[] queries) {
        ArrayList<Product> results = new ArrayList<>();
        String queryString = array2String(queries);
        
        connect();
        tell("SearchProducts", queryString);
        String productInfo;
        while ((productInfo = listen()) != null) {
            Product product = new Product();
            product.readFromLine(productInfo);
            results.add(product);
        }
        disconnect();
        
        return results;
    }
    
    public ArrayList<Product> search(ArrayList<String> queries) {
        return search((String[]) queries.toArray());
    }
    
    private String array2String(String[] v) {
        String string = "";
        for (int i = 0; i < v.length; i++) {
            string += v[i];
            if (i < v.length-1)
                string += "\t";
        }
        
        return string;
    }
    
    public boolean signup(String username, String password, String email) {
        this.user = new ShopUser(username, password, email, 0, 0);
        
        connect();
        tell("SignUp", user);
        String response = listen();
        System.out.println(response);
        disconnect();
        
        return response.equals("SignUp Successful");
    }
    
    public boolean login(String username, String password) {
        this.user = new ShopUser(username, password, null, 0, 0);
        
        connect();
        tell("Login", user);
        String response = listen();
        boolean success;
        System.out.println(response);
        if (success = response.contains("Login Successful"))
            user.readFromLine(response, 1);
        disconnect();
        return success;
    }
    
    public boolean addProduct(Product product) {
        connect();
        tell("AddProduct", user, product);
        String response = listen();
        System.out.println(response);
        disconnect();
        
        return response.equals("AddProduct Successful");
    }
    
    public boolean updateProduct(Product product) {
        connect();
        tell("UpdateProduct", user, product);
        String response = listen();
        System.out.println(response);
        disconnect();
        
        return response.equals("UpdateProduct Successful");
    }
    
    public boolean addReview(Product product, Review review) {
        connect();
        tell("AddReview", user, product.getName(), review);
        String response = listen();
        System.out.println(response);
        disconnect();
        
        return response.equals("AddReview Successful");
    }
    
    public boolean updateUserStatus(String username, int newLevel) {
        connect();
        tell("UpdateUserStatus", user, username, newLevel);
        String response = listen();
        System.out.println(response);
        disconnect();
        
        return response.equals("UpdateUserStatus Successful");
    }
    
    
    public boolean addReview(String productName, String comment, int star) {
        return addReview(new Product(productName, "", 0, 0, 0, 0), new Review(user.getUsername(), comment, star));
    }
    
    public ArrayList<Product> getSellerProducts(String sellerUsername) {
        connect();
        tell("GetSellerProducts", sellerUsername);
        ArrayList<Product> products = new ArrayList<>();
        String productInfo;
        
        while ((productInfo = listen()) != null) {
            Product product = new Product();
            product.readFromLine(productInfo);
            products.add(product);
        }
        disconnect();
        return products;
    }
    
    private void connect() {
        try {
            this.socket = new Socket(hostName, portNumber);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        } catch (IOException e) {
            System.err.println("RequestManager.connect(): IOException");
        }
    }
    
    private void disconnect() {
        try {
            out.flush();
            out.close();
            in.close();
            this.socket.close();
        } catch (IOException e) {
            System.err.println("RequestManager.disconnect(): IOException");
        }
    }

    public String listen() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.out.println("RequestManager.listen() : IOException");
            return null;
        }
    }
    
    public void tell(Object... objects) {
        String saying = "";
        for (int i = 0; i < objects.length; i++) {
            saying += objects[i];
            if (i < objects.length-1)
                saying += "\t";
        }
        out.println(saying); // add flush if ran into problem
    }
    
    public void downloadFile(String path) {
        try {
            if (new File(path).isFile())
                new File(path).delete();
            Files.copy(socket.getInputStream(), Path.of(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("RequestManager.downloadFile() : IOException");
        }
    }
    
    public void uploadFile(String path) {
        try {
            Files.copy(Path.of(path), socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("RequestManager.uploadFile(): IOException");
        }
    }
}
