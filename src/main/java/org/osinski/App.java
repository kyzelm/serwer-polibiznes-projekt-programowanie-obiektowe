package org.osinski;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class App {
    private static final String[][] gameData = {
            {
                    "GAMESTATE", // [0][0] - action
                    "", // [0][1] - addicional data
            },
            {   // player 1
                    "", // [1][0] - address
                    "", // [1][1] - port
                    "", // [1][2] - nickname
                    "", // [1][3] - money
                    "", // [1][4] - position
                    "", // [1][5] - jail time
                    "", // [1][6] - properties
            },
            {   // player 2
                    "", // [2][0] - address
                    "", // [2][1] - port
                    "", // [2][2] - nickname
                    "", // [2][3] - money
                    "", // [2][4] - position
                    "", // [2][5] - jail time
                    "", // [2][6] - properties
            },
            {   // player 3
                    "", // [3][0] - address
                    "", // [3][1] - port
                    "", // [3][2] - nickname
                    "", // [3][3] - money
                    "", // [3][4] - position
                    "", // [3][5] - jail time
                    "", // [3][6] - properties
            },
            {   // player 4
                    "", // [4][0] - address
                    "", // [4][1] - port
                    "", // [4][2] - nickname
                    "", // [4][3] - money
                    "", // [4][4] - position
                    "", // [4][5] - jail time
                    "", // [4][6] - properties
            },
    };

    private static final int PORT = 2137;
    private static DatagramSocket socket;

    public static void main(String[] args) {
        int actualPlayer = 1;

        try {
            socket = new DatagramSocket(PORT);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + message);

                String[] clientMessages = message.split("\t");

                switch (clientMessages[0]) {
                    case "JOIN":
                        for (int i = 1; i < gameData.length; i++) {
                            if (gameData[i][0].isEmpty()) {
                                gameData[i][0] = packet.getAddress().getHostAddress();
                                gameData[i][1] = String.valueOf(packet.getPort());
                                gameData[i][2] = clientMessages[1];
                                gameData[i][3] = "1500";
                                gameData[i][4] = "0";
                                gameData[i][5] = "0";
                                gameData[i][6] = "";


                                for (int j = 1; j < gameData.length; j++) {
                                    if (i == 4 && j == actualPlayer) {
                                        gameData[0][0] = "ROLL";
                                    } else {
                                        gameData[0][0] = "GAMESTATE";
                                    }

                                    if (!gameData[j][0].isEmpty() && !gameData[j][1].isEmpty()) {
                                        send(j);
                                    }
                                }
                                break;
                            }
                        }
                        break;
                    case "ROLL":
                        break;
                    case "BUY":
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(int playerId) throws UnknownHostException {
        String message = "";
        for (String[] playerData : gameData) {
            for (String data : playerData) {
                message += data + "\t";
            }
            message += "\n";
        }

        System.out.println("Sending: " + message);

        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(gameData[playerId][0]), Integer.parseInt(gameData[playerId][1]));
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
