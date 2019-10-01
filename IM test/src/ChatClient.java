import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A Swing-based client GUI for the chat server.
 */
public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Jays Chat");
    JTextField textWindow = new JTextField(20);
    JTextArea messageWindow = new JTextArea(20, 40);
    JButton bClear, bSend;

    /**
     * ChatClient constructor
     */
    public ChatClient() {

        //GUI Layout
        Font h = new Font("Helvetica", Font.PLAIN, 18);
        this.textWindow.setEditable(false);
        this.messageWindow.setEditable(false);
        this.messageWindow.setFont(h);

        //Initialize JButton
        this.bClear = new JButton("Clear");
        this.bSend = new JButton("Send");

        //position of the panel
        this.frame.getContentPane().add(new JScrollPane(this.messageWindow),
                BorderLayout.NORTH);
        this.frame.getContentPane().add(this.textWindow, BorderLayout.CENTER);
        this.frame.getContentPane().add(this.bClear, BorderLayout.EAST);
        this.frame.getContentPane().add(this.bSend, BorderLayout.SOUTH);

        this.frame.pack();

        //Action Listener with send features
        ActionListener send = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatClient.this.out
                        .println(ChatClient.this.textWindow.getText());
                ChatClient.this.textWindow.setText("");
            }
        };

        //Action Listener with clear message box features
        ActionListener clear = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChatClient.this.messageWindow.setText("");
            }
        };
        this.bClear.addActionListener(clear);
        this.bSend.addActionListener(send);
        this.textWindow.addActionListener(send);

    }

    /**
     * Prompt user a window to enter the IP Address
     */
    private String getServerAddress() {
        String ip = JOptionPane.showInputDialog(this.frame,
                "Enter IP Address of the Server:", "Welcome to the Jays Chat",
                JOptionPane.QUESTION_MESSAGE);
        return ip;
    }

    /**
     * Prompt user a window to enter username.
     */

    private String getID() {
        String id = JOptionPane.showInputDialog(this.frame, "Username", "Login",
                JOptionPane.QUESTION_MESSAGE);

        return id;
    }

    /**
     * Prompt user a window to enter password.
     */
    private String getPass() {

        String password = JOptionPane.showInputDialog(this.frame, "Password",
                "Login", JOptionPane.QUESTION_MESSAGE);

        return password;
    }

    /**
     * Prompt user a window to enter confirm password.
     */
    private String getConfirmPass() {
        String cpassword = JOptionPane.showInputDialog(this.frame,
                "Confirm Password", "Login", JOptionPane.QUESTION_MESSAGE);

        return cpassword;
    }

    /**
     * Connects to the server
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = this.getServerAddress();
        Socket socket = new Socket(serverAddress, 8888);

        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        ArrayList<String> onlineUser = new ArrayList<String>();

        String onlineID = "";

        // Process all messages from server, according to the protocol.
        while (true) {

            String line = this.in.readLine();
            if (line.startsWith("HOME")) {
                //if receive message "HOME" from server
                String[] options = { "Sign up", "Sign in" };
                int x = JOptionPane.showOptionDialog(null, "Welcome",
                        "Home Menu", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, options,
                        options[0]);
                System.out.println(x);

                if (x == 0) {
                    //When client clicked the "Sign up" button
                    //send a message to server to sign up
                    this.out.println("SIGNUP");

                } else if (x == 1) {
                    //When client clicked the "Sign in" button
                    //send a message to server to sign in
                    this.out.println("SIGNIN");
                }

            } else if (line.startsWith("SUBMITNEWNAME")) {
                //When server ask for info of new user
                //send the username, password and confirmed password
                this.out.println(this.getID());
                this.out.println(this.getPass());
                this.out.println(this.getConfirmPass());

            } else if (line.startsWith("PASSWORDDIFF")) {
                //When server detect a password difference
                //Prompt user a error window
                System.out.print(JOptionPane.showConfirmDialog(null,
                        "Password must be the same as Confirmed Password",
                        "Error", JOptionPane.DEFAULT_OPTION));

            } else if (line.startsWith("SUBMITNAME")) {
                //When server ask for info of old user
                //send the username and password
                onlineID = this.getID();
                this.out.println(onlineID);
                this.out.println(this.getPass());

            } else if (line.startsWith("SIGNUPSUCCESS")) {
                //prompt user a "Sign up successfully" window
                System.out.print(JOptionPane.showConfirmDialog(null,
                        "New chat account created", "Welcome",
                        JOptionPane.DEFAULT_OPTION));
            } else if (line.startsWith("LOGGEDIN")) {
                //prompt user a "Logged in successfully" window and make the text box available
                System.out.println(JOptionPane.showConfirmDialog(null,
                        "Logged in Successfully", "Welcome",
                        JOptionPane.DEFAULT_OPTION));

                this.textWindow.setEditable(true);

            } else if (line.startsWith("EMPTYUSERPASS")) {
                //prompt user a error window
                System.out.println(JOptionPane.showConfirmDialog(null,
                        "Invalid Username and Password", "Error",
                        JOptionPane.DEFAULT_OPTION));

            } else if (line.startsWith("SIGNEDUPERROR")) {
                //prompt user a sign in error window
                int signInError = JOptionPane.showConfirmDialog(null,
                        "Username Already Exist", "Error",
                        JOptionPane.DEFAULT_OPTION);
                System.out.println(signInError);

            } else if (line.startsWith("WRONGPASSWORD")) {
                //prompt user a "worng password" error window
                int wrongPass = JOptionPane.showConfirmDialog(null,
                        "Wrong username or password", "Error",
                        JOptionPane.DEFAULT_OPTION);
                System.out.println(wrongPass);

            } else if (line.startsWith("MESSAGE")) {
                //print client message to the message box
                String msg = line.substring(8);

                this.messageWindow.append(msg + "\n");

            } else if (line.startsWith("CHECKONLINE")) {
                //check who is online
                for (int i = 0; i < onlineUser.size(); i++) {
                    this.messageWindow
                            .append(onlineUser.get(i) + " is online." + "\n");
                }
            } else if (line.startsWith("SAVEID")) {
                //save online client in an array list
                onlineUser.add(line.substring(6));

            } else if (line.startsWith("REMOVEID")) {
                //remove offline client from the array list
                onlineUser.remove(line.substring(8));
            } else if (line.startsWith("LOGOUT")) {
                //prompt client a window to choose if they want to close the chat window
                int option = JOptionPane.showConfirmDialog(this.frame,
                        "Going offline. Do you want to close the chat window?",
                        "Quit", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            } else if (line.startsWith("CLEARARRAY")) {
                //clear online user array so that it refresh everytime someone logged in
                onlineUser.clear();
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}