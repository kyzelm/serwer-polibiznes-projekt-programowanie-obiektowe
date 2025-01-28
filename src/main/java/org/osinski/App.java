package org.osinski;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * Serwer gry obsługujący logikę gry Polibiznes.
 */
public class App {
    private static String[][] gameData;

    private static final int PORT = 2137;
    private static DatagramSocket socket;

    private static File file;
    private static FileWriter fileWriter;

    private static int playerCount = 0;
    private static int actualPlayer = 1;
    private static int loses = 0;

    /**
     * Metoda główna serwera gry.
     *
     * @param args argumenty wywołania programu
     */
    public static void main(String[] args) {
        reset();


        try {
            file = new File("gameData.txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead = fileInputStream.read(buffer);
            if (bytesRead > 0) {
                String message = new String(buffer, 0, bytesRead);
                String[] messages = message.split("\n");
                for (int i = 0; i < messages.length; i++) {
                    String[] playerData = messages[i].split("\t");
                    for (int j = 0; j < playerData.length; j++) {
                        gameData[i][j] = playerData[j];
                    }
                }

                actualPlayer = Integer.parseInt(gameData[0][2]);
            }

            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                        playerCount++;
                        if (playerCount > 4) {
                            playerCount = 4;
                            byte[] response = "FULL".getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                            socket.send(responsePacket);
                            break;
                        }
                        if (gameData[playerCount][0].isEmpty()) {
                            for (int i = 1; i < playerCount; i++) {
                                if (Objects.equals(gameData[i][2], clientMessages[1])) {
                                    playerCount--;
                                    byte[] response = "NICK".getBytes();
                                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                                    socket.send(responsePacket);
                                    break;
                                }
                            }

                            gameData[playerCount][0] = packet.getAddress().getHostAddress();
                            gameData[playerCount][1] = String.valueOf(packet.getPort());
                            gameData[playerCount][2] = clientMessages[1];
                            gameData[playerCount][3] = "1500";
                            gameData[playerCount][4] = "0";
                            gameData[playerCount][5] = "0";
                        } else {
                            boolean nickDetected = false;
                            for (int i = 1; i < 5; i++) {
                                if (Objects.equals(gameData[i][2], clientMessages[1])) {
                                    gameData[i][0] = packet.getAddress().getHostAddress();
                                    gameData[i][1] = String.valueOf(packet.getPort());
                                    nickDetected = true;
                                }
                            }
                            if (!nickDetected) {
                                playerCount--;
                                byte[] response = "FULL".getBytes();
                                DatagramPacket responsePacket = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                                socket.send(responsePacket);
                            }
                        }

                        for (int i = 1; i < 5; i++) {
                            if (gameData[i][0].isEmpty()) {
                                continue;
                            }
                            if (playerCount == 4 && i == 1) {
                                gameData[0][0] = "ROLL";
                            }
                            try {
                                send(i);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }

                        break;
                    case "ROLL":
                        int newPosition = Integer.parseInt(gameData[actualPlayer][4]) + Integer.parseInt(clientMessages[1]);
                        if (newPosition > 25) {
                            newPosition -= 25;
                            gameData[actualPlayer][3] = String.valueOf(Integer.parseInt(gameData[actualPlayer][3]) + 200);
                        }
                        gameData[actualPlayer][4] = String.valueOf(newPosition);
                        if (newPosition == 20) {
                            gameData[actualPlayer][5] = "1";
                            gameData[actualPlayer][4] = "7";
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                            if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                            }
                            if (Objects.equals(gameData[actualPlayer][5], "1")) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                                gameData[actualPlayer][5] = "0";
                            }
                            gameData[0][2] = String.valueOf(actualPlayer);
                            for (int i = 1; i < 5; i++) {
                                if (gameData[i][0].isEmpty()) {
                                    continue;
                                }
                                if (actualPlayer == i) {
                                    gameData[0][0] = "ROLL";
                                }
                                try {
                                    send(i);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        if (newPosition == 3 || newPosition == 10 || newPosition == 16 || newPosition == 23) {
                            gameData[actualPlayer][3] = String.valueOf(Integer.parseInt(gameData[actualPlayer][3]) + 300);
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                            if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                            }
                            if (Objects.equals(gameData[actualPlayer][5], "1")) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                                gameData[actualPlayer][5] = "0";
                            }
                            gameData[0][2] = String.valueOf(actualPlayer);
                            for (int i = 1; i < 5; i++) {
                                if (gameData[i][0].isEmpty()) {
                                    continue;
                                }
                                if (actualPlayer == i) {
                                    gameData[0][0] = "ROLL";
                                }
                                try {
                                    send(i);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        if (gameData[newPosition + 5][2].equals("x")) {
                            gameData[0][1] = String.valueOf(newPosition);
                            for (int i = 1; i < 5; i++) {
                                if (gameData[i][0].isEmpty()) {
                                    continue;
                                }
                                if (actualPlayer == i) {
                                    gameData[0][0] = "BUY";
                                }
                                try {
                                    send(i);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        if (!gameData[newPosition + 5][2].equals("x") && !gameData[newPosition + 5][2].equals("o")) {
                            if (!gameData[Integer.parseInt(gameData[newPosition + 5][2])][5].equals("1") || !(Integer.parseInt(gameData[Integer.parseInt(gameData[newPosition + 5][2])][3]) <= 0)) {
                                gameData[actualPlayer][3] = String.valueOf(Integer.parseInt(gameData[actualPlayer][3]) - (Integer.parseInt(gameData[newPosition + 5][1])));
                                gameData[Integer.parseInt(gameData[newPosition + 5][2])][3] = String.valueOf(Integer.parseInt(gameData[Integer.parseInt(gameData[newPosition + 5][2])][3]) + (Integer.parseInt(gameData[newPosition + 5][1])));

                                if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                                    loses++;
                                }
                            }
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                            if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                            }
                            if (Objects.equals(gameData[actualPlayer][5], "1")) {
                                actualPlayer++;
                                if (actualPlayer > 4) {
                                    actualPlayer = 1;
                                }
                                gameData[actualPlayer][5] = "0";
                            }
                            gameData[0][2] = String.valueOf(actualPlayer);
                            for (int i = 1; i < 5; i++) {
                                if (gameData[i][0].isEmpty()) {
                                    continue;
                                }
                                if (actualPlayer == i) {
                                    gameData[0][0] = "ROLL";
                                }
                                try {
                                    send(i);
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        }
                        actualPlayer++;
                        if (actualPlayer > 4) {
                            actualPlayer = 1;
                        }
                        if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                        }
                        if (Objects.equals(gameData[actualPlayer][5], "1")) {
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                            gameData[actualPlayer][5] = "0";
                        }
                        gameData[0][2] = String.valueOf(actualPlayer);
                        for (int i = 1; i < 5; i++) {
                            if (gameData[i][0].isEmpty()) {
                                continue;
                            }
                            if (actualPlayer == i) {
                                gameData[0][0] = "ROLL";
                            }
                            try {
                                send(i);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "BUY":
                        if (Objects.equals(clientMessages[1], "YES")) {
                            System.out.println("YES");
                            gameData[actualPlayer][3] = String.valueOf(Integer.parseInt(gameData[actualPlayer][3]) - (Integer.parseInt(gameData[Integer.parseInt(gameData[0][1]) + 5][1])));
                            gameData[Integer.parseInt(gameData[0][1]) + 5][2] = String.valueOf(actualPlayer);
                            if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                                loses++;
                            }
                        }
                        actualPlayer++;
                        if (actualPlayer > 4) {
                            actualPlayer = 1;
                        }
                        if (Integer.parseInt(gameData[actualPlayer][3]) <= 0) {
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                        }
                        if (Objects.equals(gameData[actualPlayer][5], "1")) {
                            actualPlayer++;
                            if (actualPlayer > 4) {
                                actualPlayer = 1;
                            }
                            gameData[actualPlayer][5] = "0";
                        }
                        gameData[0][2] = String.valueOf(actualPlayer);
                        for (int i = 1; i < 5; i++) {
                            if (gameData[i][0].isEmpty()) {
                                continue;
                            }
                            if (actualPlayer == i) {
                                gameData[0][0] = "ROLL";
                            }
                            try {
                                send(i);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }

                if (loses == 3) {
                    for (int i = 1; i < 5; i++) {
                        if (gameData[i][0].isEmpty()) {
                            continue;
                        }
                        if (Integer.parseInt(gameData[i][3]) > 0) {
                            gameData[0][0] = "WIN";
                        } else {
                            gameData[0][0] = "LOSE";
                        }
                        send(i);
                    }
                    reset();
                }

                for (int i = 1; i < 5; i++) {
                    if (gameData[i][0].isEmpty()) {
                        continue;
                    }
                    if (Integer.parseInt(gameData[i][3]) > 10000) {
                        for (int j = 1; j < 5; j++) {
                            if (gameData[j][0].isEmpty()) {
                                continue;
                            }
                            if (Integer.parseInt(gameData[j][3]) > 0) {
                                gameData[0][0] = "WIN";
                            } else {
                                gameData[0][0] = "LOSE";
                            }
                            send(j);
                        }
                        reset();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();

            if (playerCount != 4 && file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Metoda wysyłająca dane do klienta.
     *
     * @param playerId identyfikator gracza
     * @throws UnknownHostException wyjątek rzucany w przypadku nieznanej nazwy hosta
     */
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

        gameData[0][0] = "GAMESTATE";

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(message);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metoda resetująca stan gry.
     */
    private static void reset() {
        for (int i = 1; i < 5; i++) {
            playerCount = 0;
            actualPlayer = 1;
            loses = 0;
            gameData = new String[][]{
                    {
                            "GAMESTATE", // [0][0] - action
                            "", // [0][1] - addicional data
                            "1", // [0][2] - actual player
                    },
                    {   // player 1
                            "", // [1][0] - address
                            "", // [1][1] - port
                            "", // [1][2] - nickname
                            "", // [1][3] - money
                            "", // [1][4] - position
                            "", // [1][5] - jail time
                    },
                    {   // player 2
                            "", // [2][0] - address
                            "", // [2][1] - port
                            "", // [2][2] - nickname
                            "", // [2][3] - money
                            "", // [2][4] - position
                            "", // [2][5] - jail time
                    },
                    {   // player 3
                            "", // [3][0] - address
                            "", // [3][1] - port
                            "", // [3][2] - nickname
                            "", // [3][3] - money
                            "", // [3][4] - position
                            "", // [3][5] - jail time
                    },
                    {   // player 4
                            "", // [4][0] - address
                            "", // [4][1] - port
                            "", // [4][2] - nickname
                            "", // [4][3] - money
                            "", // [4][4] - position
                            "", // [4][5] - jail time
                    },
                    {   // field 1
                            "Start", // [5][0] - name
                            "0", // [5][1] - price
                            "o", // [5][2] - owner
                    },
                    {   // field 2
                            "BHP", // [6][0] - name
                            "100", // [6][1] - price
                            "x", // [6][2] - owner
                    },
                    {   // field 3
                            "Własność intelektualna", // [7][0] - name
                            "200", // [7][1] - price
                            "x", // [7][2] - owner
                    },
                    {   // field 4
                            "Stypendium", // [8][0] - name
                            "0", // [8][1] - price
                            "o", // [8][2] - owner
                    },
                    {   // field 5
                            "Angielski", // [9][0] - name
                            "150", // [9][1] - price
                            "x", // [9][2] - owner
                    },
                    {   // field 6
                            "Miernictwo cyfrowe", // [10][0] - name
                            "150", // [10][1] - price
                            "x", // [10][2] - owner
                    },
                    {   // field 7
                            "Elektronika", // [11][0] - name
                            "200", // [11][1] - price
                            "x", // [11][2] - owner
                    },
                    {   // field 8
                            "Kolejka do dziekanatu", // [12][0] - name
                            "0", // [12][1] - price
                            "o", // [12][2] - owner
                    },
                    {   // field 9
                            "Matematyka", // [13][0] - name
                            "250", // [13][1] - price
                            "x", // [13][2] - owner
                    },
                    {   // field 10
                            "Mikrokontrolery", // [14][0] - name
                            "250", // [14][1] - price
                            "x", // [14][2] - owner
                    },
                    {   // field 11
                            "Stypendium", // [15][0] - name
                            "0", // [15][1] - price
                            "o", // [15][2] - owner
                    },
                    {   // field 12
                            "Algorytmy", // [16][0] - name
                            "300", // [16][1] - price
                            "x", // [16][2] - owner
                    },
                    {   // field 13
                            "ASK", // [17][0] - name
                            "300", // [17][1] - price
                            "x", // [17][2] - owner
                    },
                    {   // field 14
                            "Odwołane zajęcia", // [18][0] - name
                            "0", // [18][1] - price
                            "o", // [18][2] - owner
                    },
                    {   // field 15
                            "Bazy danych", // [19][0] - name
                            "350", // [19][1] - price
                            "x", // [19][2] - owner
                    },
                    {   // field 16
                            "Systemy inteligentne", // [20][0] - name
                            "350", // [20][1] - price
                            "x", // [20][2] - owner
                    },
                    {   // field 17
                            "Stypendium", // [21][0] - name
                            "0", // [21][1] - price
                            "o", // [21][2] - owner
                    },
                    {   // field 18
                            "Grafika komputerowa", // [22][0] - name
                            "400", // [22][1] - price
                            "x", // [22][2] - owner
                    },
                    {   // field 19
                            "Projektowanie układów", // [23][0] - name
                            "400", // [23][1] - price
                            "x", // [23][2] - owner
                    },
                    {   // field 20
                            "Programowanie obiektowe", // [24][0] - name
                            "450", // [24][1] - price
                            "x", // [24][2] - owner
                    },
                    {   // field 21
                            "Mail do dziekanatu", // [25][0] - name
                            "0", // [25][1] - price
                            "o", // [25][2] - owner
                    },
                    {   // field 22
                            "Technologie internetowe", // [26][0] - name
                            "500", // [26][1] - price
                            "x", // [26][2] - owner
                    },
                    {   // field 23
                            "Praktyki", // [27][0] - name
                            "500", // [27][1] - price
                            "x", // [27][2] - owner
                    },
                    {   // field 24
                            "Stypendium", // [28][0] - name
                            "0", // [28][1] - price
                            "o", // [28][2] - owner
                    },
                    {   // field 25
                            "Zajęcia dyplomowe", // [29][0] - name
                            "550", // [29][1] - price
                            "x", // [29][2] - owner
                    },
                    {   // field 26
                            "Praca inżynierska", // [30][0] - name
                            "600", // [30][1] - price
                            "x", // [30][2] - owner
                    },
            };
        }
    }
}
