package bgu.spl.net.objects;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {

    private boolean terminate;
    private String username;
    private boolean isAdmin = false;
    private String toLogout = null;

    private int connectionId;
    private Connections<T> connections;

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        terminate = false;
        this.username = null;
    }

    @Override
    public void process(T message) {
        //connections.send(id, msg)
        if (message.equals(""))
            return;

        /*
        Switch case on each opcode, call for the appropriate function
        return "10 opcode" for success, "11 opcode" for error and "9 ..."
        for notification.
         */
        String[] temp = message.toString().split(" ");
        String[] inputs = new String[temp.length - 1];
        String st = "";
        for (int i = 0; i < temp.length - 1; i++) {
            inputs[i] = temp[i + 1];
        }

        switch (getOpcode(message.toString())) {
            case 1://register
                ConnectionsImpl.getInstance().register(inputs[0], inputs[1], inputs[2], connectionId);
                break;
            case 2://login
                if (username == null) {
                    st = ConnectionsImpl.getInstance().login(inputs[0], inputs[1], inputs[2], connectionId);
                    if (st != null) {
                        this.username = st;
                    }
                } else {
                    ConnectionsImpl.getInstance().login(inputs[0], inputs[1], "0", connectionId);
                }
                break;
            case 3://logout
                if (ConnectionsImpl.getInstance().logout(username, connectionId)) {
                    username = null;
                }
                break;
            case 4://follow or unfollow
                ConnectionsImpl.getInstance().followUnfollow(Integer.parseInt(inputs[0]), username, inputs[1], connectionId);
                break;
            case 5:
                st = "";
                for (int i = 1; i < inputs.length; i++) {
                    st += inputs[i] + " ";
                }
                st=st.substring(0,st.length()-1);
                ConnectionsImpl.getInstance().post(username, st, connectionId);
                break;
            case 6:
                st = "";
                for (int i = 1; i < inputs.length; i++) {
                    st += inputs[i] + " ";
                }
                st=st.substring(0,st.length()-1);
                ConnectionsImpl.getInstance().sendPM(username, inputs[0], st, connectionId);
                break;
            case 7:
                ConnectionsImpl.getInstance().logStat(username, connectionId);
                break;
            case 8:
                ConnectionsImpl.getInstance().stat(username, inputs[0], connectionId);
                break;
            case 12:
                ConnectionsImpl.getInstance().block(username, inputs[0], connectionId);
                break;

        }
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
