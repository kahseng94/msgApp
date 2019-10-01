import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A multithreaded chat room server.
 */
public class ChatServer {

    /**
     * Server Port Number
     */
    private static final int PORT = 8888;

    /**
     * The map of all username and password of clients in the server.
     */
    private static Map<String, String> IDPASS = new HashMap<String, String>();

    /**
     * The map of all username and password of clients to be printed in the text
     * file.
     */
    private static Map<String, String> bwMap = new HashMap<String, String>();

    /**
     * The array list of username of all online clients in the chat room.
     */
    private static ArrayList<String> onlineUser = new ArrayList<String>();
    /**
     * The set of all the print writers for all the clients.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    private static File datafile = new File("datafile.txt");

    private static BufferedWriter brWrite;

    /**
     * A handler thread class that send request msg and receive response msg
     */
    private static class Handler extends Thread {
        private String id;
        private String password;
        private String newID;
        private String newPassword;
        private String confirmPass;
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * Handler Constructor
         */
        private Handler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        /**
         * The main part of the sending and receiving message from client
         */
        @Override
        public void run() {
            try {
                // Create character streams for the socket.
                this.in = new BufferedReader(new InputStreamReader(
                        this.clientSocket.getInputStream()));
                this.out = new PrintWriter(this.clientSocket.getOutputStream(),
                        true);
                while (true) {
                    this.out.println("HOME");
                    //waiting for client's choice of signing up or signing in
                    String registerMsg = this.in.readLine();
                    if (registerMsg.startsWith("SIGNUP")) {
                        //if client choose to sign up

                        //Send a request msg for new info
                        this.out.println("SUBMITNEWNAME");

                        //Request username, password and confirm password from client
                        this.id = this.in.readLine();
                        this.password = this.in.readLine();
                        this.confirmPass = this.in.readLine();

                        String newPass = this.password;
                        String conPass = this.confirmPass;

                        if (!conPass.equals(newPass)) {

                            //if password and confirm password is different from each other
                            //send a message inform client about the password difference
                            this.out.println("PASSWORDDIFF");

                        } else if (this.id == " " || this.password == " "
                                || this.confirmPass == " ") {

                            //if id, password and confirm password is null
                            this.out.println("EMPTYUSERPASS");

                        } else {

                            if (!IDPASS.containsKey(this.id)) {
                                //if username is new, add the username and password into the file
                                bwMap.put(this.id, this.password);

                                //create a new bufferwriter to store id and password
                                brWrite = new BufferedWriter(
                                        new OutputStreamWriter(
                                                new FileOutputStream(datafile,
                                                        true)));

                                String key = this.id;
                                String value = this.password;
                                //write id and password in the text file
                                brWrite.write(key + " " + value + "\r\n");
                                brWrite.close();

                                IDPASS.put(this.id, this.password);
                                this.out.println("SIGNUPSUCCESS");

                            } else {

                                this.out.println("SIGNEDUPERROR");

                            }
                        }
                    } else if (registerMsg.startsWith("SIGNIN")) {

                        this.out.println("SUBMITNAME");
                        this.id = this.in.readLine();
                        this.password = this.in.readLine();

                        if (this.id == null || this.password == null) {

                            //if id, password and confirm password is null
                            this.out.println("EMPTYUSERPASS");

                        } else {

                            String typedPass = this.password;
                            String pass = IDPASS.get(this.id);

                            if (IDPASS.containsKey(this.id)
                                    && typedPass.equals(pass)) {
                                //if the map set contains username
                                //and typed password must be the same as the password store in the map
                                this.out.println("LOGGEDIN");
                                //add client to printwriter
                                writers.add(this.out);
                                onlineUser.add(this.id);
                                break;
                            } else {
                                //if password are not the same
                                //send a msg to client about the wrong msg
                                this.out.println("WRONGPASSWORD");
                            }
                        }
                    }

                }

                // Accept messages from this client and broadcast them.
                while (true) {
                    //wait for client to type message
                    String input = this.in.readLine();

                    if (input == null) {
                        return;
                    }

                    if (input.equals("-online")) {
                        this.out.println("CLEARARRAY");
                        //add all the online user's name into arraylist
                        for (int i = 0; i < onlineUser.size(); i++) {
                            this.out.println("SAVEID" + onlineUser.get(i));
                        }
                        //send "CheckOnline" msg to client
                        this.out.println("CHECKONLINE");

                    } else if (input.equals("-quit")) {
                        // if client typed "-quit"

                        //remove username from maps
                        this.out.println("REMOVEID" + this.id);
                        onlineUser.remove(this.id);

                        //send client "Logout" msg
                        this.out.println("LOGOUT");
                        //print client's offine status to all client's messaging window
                        for (PrintWriter writer : writers) {
                            writer.println(
                                    "MESSAGE " + this.id + " is offline.");
                        }
                        //close client socket
                        this.clientSocket.close();

                    } else {

                        //for each loop is to print the message for all the users
                        for (PrintWriter writer : writers) {
                            //setting up msg time
                            SimpleDateFormat formatter = new SimpleDateFormat(
                                    "HH:mm:ss");
                            Date date = new Date();

                            writer.println("MESSAGE " + formatter.format(date)
                                    + " " + this.id + ": " + input);

                        }
                    }
                }
            } catch (IOException e) {
                //print exception
                System.out.println(e);

            } finally {
                try {
                    //close the client socket
                    this.clientSocket.close();
                } catch (IOException e) {
                    //print exception
                    System.out.println(e);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("The chat server is running.");

        if (datafile.exists()) {
            //if the text file exists, read the username and password and add them into a map
            BufferedReader br = new BufferedReader(new FileReader(datafile));
            String str;
            while ((str = br.readLine()) != null) {
                String[] map = str.split(" ");
                IDPASS.put(map[0], map[1]);
            }
        }
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Handler(clientSocket).start();
            }

        } finally {
            serverSocket.close();
        }
    }
}