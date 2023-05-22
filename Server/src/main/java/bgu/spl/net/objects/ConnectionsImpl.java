package bgu.spl.net.objects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer, bgu.spl.net.srv.ConnectionHandler<T>> active;
    private ConcurrentHashMap<String, Integer> userId;
    private ConcurrentHashMap<String, UserData> users;
    private ConcurrentHashMap<String, LinkedBlockingQueue<String>> following;
    private ConcurrentHashMap<String, LinkedBlockingQueue<String>> followers;
    private ConcurrentHashMap<String, LinkedBlockingQueue<String>> unseenPosts;
    private ConcurrentHashMap<String, LinkedBlockingQueue> blockList;
    private AtomicInteger id;
    private final ArrayList<String> censoredWords;
    private static ConnectionsImpl connections = null;

    private ConnectionsImpl() {
        users = new ConcurrentHashMap<String, UserData>();
        active = new ConcurrentHashMap<Integer, bgu.spl.net.srv.ConnectionHandler<T>>();
        userId = new ConcurrentHashMap<String, Integer>();
        following = new ConcurrentHashMap<String, LinkedBlockingQueue<String>>();
        followers = new ConcurrentHashMap<String, LinkedBlockingQueue<String>>();
        unseenPosts = new ConcurrentHashMap<>();
        blockList = new ConcurrentHashMap<>();
        id = new AtomicInteger(0);
        censoredWords = new ArrayList<>();
        
        try {
            Reader file = new FileReader("./filtered_words.txt");
            BufferedReader bufReader = new BufferedReader(file);

            String line = bufReader.readLine();
            while (line != null) {
                censoredWords.add(line);
                line = bufReader.readLine();
            }
            bufReader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ConnectionsImpl getInstance() {
        if (connections == null)
            connections = new ConnectionsImpl();
        return connections;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (active.containsKey(connectionId)) {
            active.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }

    public void addActive(int connectionId, bgu.spl.net.srv.ConnectionHandler<T> connectionHandler) {
        active.putIfAbsent(connectionId, connectionHandler);
    }

    public ConcurrentHashMap<Integer, bgu.spl.net.srv.ConnectionHandler<T>> getActive() {
        return active;
    }

    public int getId() {
        this.id.incrementAndGet();
        return id.get();
    }

    public ConcurrentHashMap<String, UserData> getUsers() {
        return users;
    }

    public void register(String username, String password, String date, int connectionId) {
        if (getUsers().containsKey(username)) {
            this.send(connectionId, (T) "11 1");
        } else {
            UserData userData = new UserData(username, password, date);
            users.putIfAbsent(username, userData);
            following.putIfAbsent(username, new LinkedBlockingQueue<>());
            followers.putIfAbsent(username, new LinkedBlockingQueue<>());
            unseenPosts.putIfAbsent(username, new LinkedBlockingQueue<>());
            blockList.put(username, new LinkedBlockingQueue<>());
            this.send(connectionId, (T) "10 1");
        }
    }

    public String login(String username, String password, String captcha, int connectionId) {
        if (captcha.equals("0")) {
            send(connectionId, (T) "11 2");
            return null;
        }
        if (users.containsKey(username)) {
            if (getUsers().get(username).getPassword().equals(password) && !users.get(username).isOnline()) {
                getUsers().get(username).setOnline(true);
                userId.put(username, connectionId);
                this.send(connectionId, (T) "10 2");
                for (String msg : unseenPosts.get(username)) {
                    this.send(connectionId, (T) msg);
                }
                unseenPosts.get(username).clear();
                return username;
            } else {
                this.send(connectionId, (T) "11 2");
                return null;
            }
        } else {
            this.send(connectionId, (T) "11 2");
            return null;
        }
    }

    public boolean logout(String username, int connetionId) {
        if (username!=null&&users.containsKey(username) && users.get(username).isOnline()) {
            users.get(username).setOnline(false);
            this.send(connetionId, (T) "10 3");
            return true;
        } else {
            this.send(connetionId, (T) "11 3");
            return false;
        }
    }

    public void followUnfollow(int follow, String follower, String toFollow, int conectionId) {
        if (follower==null || follower.equals(toFollow)) {
            this.send(conectionId, (T) "11 4");
        } else if (follow == 0) {
            if (users.containsKey(follower) && users.containsKey(toFollow) && !following.get(follower).contains(toFollow) && !isBlocked(follower, toFollow)) {
                following.get(follower).add(toFollow);
                followers.get(toFollow).add(follower);
                this.send(conectionId, (T) "10 4");
            } else {
                this.send(conectionId, (T) "11 4");
            }
        } else {
            if (users.containsKey(follower) && users.containsKey(toFollow) && following.get(follower) != null && following.get(follower).contains(toFollow) && !isBlocked(follower, toFollow)) {
                following.get(follower).remove(toFollow);
                followers.get(toFollow).remove(follower);
                this.send(conectionId, (T) "10 4");
            } else {
                this.send(conectionId, (T) "11 4");
            }
        }
    }

    public void post(String username, String content, int connectionId) {
        if (username !=null && users.containsKey(username) && users.get(username).isOnline()) {
            LinkedBlockingQueue<String> list = followers.get(username);
            for (String follower : list) {
                if (users.get(follower).isOnline()) {
                    this.send(userId.get(follower), (T) ("9 1 " + username + " " + content));

                } else {
                    unseenPosts.get(follower).add("9 1 " + username + " " + content);
                }
            }
            users.get(username).setNumOfPosts(users.get(username).getNumOfPosts() + 1);
            checkTagsAndSend(username, content);
            this.send(connectionId, (T) "10 5");
        } else
            this.send(connectionId, (T) "11 5");
    }

    private void checkTagsAndSend(String sender, String content) {
        int start = content.indexOf("@");
        while (start >= 0) {
            int end = content.indexOf(" ", start);
            if(end==-1)
                end=content.length();
            String user = content.substring(start + 1, end);
            if (users.containsKey(user)&&!isBlocked(user, sender)) {
                if ( !followers.get(sender).contains(user)) {
                    if (users.get(sender).isOnline())
                        this.send(userId.get(user), (T) ("9 0 " + sender + " " + content));
                    else
                        unseenPosts.get(user).add("9 0 " + sender + " " + content);
                }
            }
            start = content.indexOf("@", end);
        }
    }

    public void sendPM(String username, String reciever, String content, int connectionId) {
        int size = censoredWords.size();
        for(int i = 0 ; i < size ; i++){
            content = content.replaceAll( censoredWords.get(i), "<filtered>");
        }
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        if (username!=null&&users.containsKey(username) && users.containsKey(reciever) && users.get(username).isOnline() && !isBlocked(username, reciever) && following.get(username).contains(reciever)) {
            String message="9 0 " + username + " " + content +" "+dtf.format(now);
            if (users.get(reciever).isOnline()) {
                this.send(userId.get(reciever), (T) (message));
            } else
                unseenPosts.get(reciever).add((message));
            this.send(connectionId, (T) "10 6");
        } else
            this.send(connectionId, (T) "11 6");
    }

    public void logStat(String username, int connectionId) {
        if (username!=null&&users.containsKey(username) && users.get(username).isOnline()) {
            for (String user : users.keySet()) {
                if (users.get(user).isOnline()) {
                    if (!isBlocked(user, username) && !isBlocked(username, user)) {
                        UserData temp = users.get(user);
                        this.send(connectionId, (T) ("10 7 " + temp.getAge() + " " + temp.getNumOfPosts() + " " + followers.get(user).size() + " " + following.get(user).size()));
                    }
                }
            }
        } else {
            this.send(connectionId, (T) "11 7");
        }
    }

    public void stat(String username, String usersStat, int connectionId) {
        if (username!=null&&users.containsKey(username) && users.get(username).isOnline()) {
            String[] userList = usersStat.split("\\|");
            for(String temp: userList){
                if(!users.containsKey(temp)){
                    this.send(connectionId, (T) "11 8");
                    return;
                }
            }
            for (String toGetStat : userList) {

                    if (!isBlocked(username, toGetStat) && !isBlocked(toGetStat , username)) {
                        UserData temp = users.get(toGetStat);
                        this.send(connectionId, (T) ("10 8 " + temp.getAge() + " " + temp.getNumOfPosts() + " " + followers.get(toGetStat).size() + " " + following.get(toGetStat).size()));
                    }else{
                        this.send(connectionId, (T) "11 8");
                    }
                
            }
        } else {
            this.send(connectionId, (T) "11 8");
        }
    }


    public void block(String blocker, String blocked, int connectionId) {
        if (blocker!=null&&users.containsKey(blocked) && users.containsKey(blocker) && !blockList.get(blocker).contains(blocked)) {
            blockList.get(blocker).add(blocked);
            blockList.get(blocked).add(blocker);
            followers.get(blocked).remove(blocker);
            following.get(blocker).remove(blocked);
            followers.get(blocker).remove(blocked);
            following.get(blocked).remove(blocker);
            this.send(connectionId, (T) "10 12");
        } else {
            this.send(connectionId, (T) "11 12");
        }

    }

    private boolean isBlocked(String blocker, String blocked) {
        return blockList.get(blocked).contains(blocker);
    }

}
