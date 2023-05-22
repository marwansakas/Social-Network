package bgu.spl.net.impl;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.objects.ConnectionsImpl;
import bgu.spl.net.objects.UserData;

public class MessagingProtocolImpl<T> implements MessagingProtocol {
    private boolean terminate = false;
    private String username = null;
    private boolean isAdmin = false;
    private String toLogout = null;

    public MessagingProtocolImpl() {
    }

    @Override
    public Object process(Object msg) {
        System.out.println("Received message:");
        System.out.println(msg.toString());
        if (msg.equals(""))
            return null;

        /*
        Switch case on each opcode, call for the appropriate function
        return "10 opcode" for success, "11 opcode" for error and "9 ..."
        for notification.
         */
        String[] temp = msg.toString().split(" ");

        String[] inputs = temp[1].split("\n");

        String username;
        String password;
        String date;
        String captcha;
        switch (getOpcode(msg.toString())) {
            case 1://register

                return "11 1";
            case 2://login

                return "11 2";
            case 3://logout

                return "11 3";
            case 4://follow or unfollow


                return "11 4";
            case 5:

                return "11 5";
            case 6:

                return "11 6";
            case 7:

                return "11 7";
            case 8:

                return "11 8";
            case 12:

                return "11 12";
        }
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

    private int getOpcode(String msg) {
        String[] temp = msg.split(" ");
        return Integer.parseInt(temp[0]);
    }


}
