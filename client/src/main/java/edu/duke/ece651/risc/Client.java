package edu.duke.ece651.risc;

import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client extends Thread {
    String name;
    private Scanner sc=new Scanner(System.in);
    private ObjectOutputStream os1 = null;
    private ObjectInputStream is1 = null;
    final int totalsoldier=9;

    public String get_name(){
        return this.name;
    }
    @Override
    public void run() {
        Socket socket = null;
        Executor end_helper=new Executor();
        try {
            socket = new Socket("localhost", 8000);
            System.out.println("Client Connected");
            this.os1 = new ObjectOutputStream(socket.getOutputStream());
            this.is1 = new ObjectInputStream(socket.getInputStream());



            //receive player
            Player player = (Player) is1.readObject();
            setName(player.getId());
            //receive map to be completed
            Map<String, Territory> territories = (Map<String, Territory>) is1.readObject();
//new Added!
            //fill the map in player
            HashMap<String,Integer> init_info=new HashMap<>();
            player.initial_game(territories,sc,totalsoldier,init_info);
            //send it to server
            os1.writeObject(init_info);
            os1.flush();
            os1.reset();
            //wait server send back completed map
            territories = (Map<String, Territory>) is1.readObject();
            //now game begins
            while(true) {
                if(!end_helper.singlePlayerFail(territories,player.getId()))
                    player.addAction(territories, get_name(),sc);
                else
                    player.addAction_afterFail(territories);
                //send player to server
                os1.writeObject(player);
                os1.flush();
                os1.reset();
                //waiting for server's reply of validating
                while (true) {
                    //System.out.println("receiving the player object from server");
                    Player temp = (Player) is1.readObject();
                    if (temp.isvalid) {
                        System.out.println("Waiting for other players'input....");
                        //System.out.println("------------------------");
                        break;
                    }
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                    System.out.println("Collision! Type again");
                    System.out.println("!!!!!!!!!!!!!!!!!!!!!!");
                    player.addAction(territories, get_name(),sc);
                    os1.writeObject(player);
                    os1.flush();
                    os1.reset();
                }
                territories = (Map<String, Territory>) is1.readObject();
                if(end_helper.checkWin(territories)) break;
            }


            //tell server it will close the client
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            String msg="exit";
            pw.println(msg);
            pw.flush();

            //close resources
            pw.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                System.out.println("Closing Client Socket");
                assert socket != null;
                socket.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setName(int id) {
        this.name = String.valueOf(id);
    }

    public static void main(String[] args) throws Exception {
        Client c = new Client();
        c.start();
    }

}









