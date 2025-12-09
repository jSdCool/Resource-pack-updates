package org.cbigames.resourceupdate;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import net.minecraft.network.chat.Component;

public class Config {
    private String hash;
    boolean onLocalComputer=true;
    String location="";
    boolean autoReCheckHash=true;
    private long lct=0;

    public Config(){
        //read file things here
        try (Scanner configFile = new Scanner(new File("config/resourcePack.cfg"))){
            while(configFile.hasNextLine()){
                String line = configFile.nextLine();
                if(line.startsWith("onLocalComputer=")){
                    onLocalComputer = line.substring("onLocalComputer=".length()).equalsIgnoreCase("true");
                }else if(line.startsWith("location=")){
                    location = line.substring("location=".length());
                }else if(line.startsWith("automaticRecheckHash=")){
                    autoReCheckHash = line.substring("automaticRecheckHash=".length()).equalsIgnoreCase("true");
                }
            }
        } catch (FileNotFoundException e) {
            save();
        }

    }

    void calculateHash(){

        byte[] rawPack;
        if(onLocalComputer){
            try(FileInputStream fis = new FileInputStream(location)){
                lct = new File(location).lastModified();
                rawPack = fis.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("failed to load resource pack file",e);
            }
        }else {
            //do a web request here
            rawPack= downloadResourcePackFromInternet();
        }
        String oldHash= hash;
        hash = toSHA1(rawPack);
        if(oldHash !=null && ! oldHash.equals(hash)){
            //if changed then announce it to the server
            ResourcePackUpdates.pm.broadcastSystemMessage(Component.literal("Server Resource pack updated. Relog to apply update"),false);
        }
    }

    private String toSHA1(byte[] convertme) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch(NoSuchAlgorithmException e) {
           return null;
        }
        return byteArrayToHexString(md.digest(convertme));
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public String getHash(){
        if(autoReCheckHash){
            if(onLocalComputer) {
                //if it was modified since last check
                if(new File(location).lastModified() != lct) {
                    calculateHash();
                }
            }else {
                //we have no way of knowing when a pack stored soley on a remote server was modified, so we must download it our self to check the ahs each time
                //this is incredibly inefficient, please have autoReCheckHash off is using this option
                calculateHash();
            }
        }
        return hash;
    }

    private void save(){
        try (PrintWriter ouput = new PrintWriter("config/resourcePack.cfg")){
            ouput.println("onLocalComputer="+onLocalComputer);
            ouput.println("location="+location);
            ouput.println("automaticRecheckHash="+autoReCheckHash);
            ouput.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] downloadResourcePackFromInternet(){
        byte[] result;
        try {
            URL   url = new URL(location);
            URLConnection c = url.openConnection();
            c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");

            InputStream input;
            input = c.getInputStream();
            //how could this go wrong?
            result = input.readAllBytes();

            input.close();

        } catch (IOException e) {
            throw new RuntimeException("An error occurred while attempting to verify the hash of the server resource pack(downloading the pack)",e);
        }
        return result;
    }


}
