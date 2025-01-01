Write a chat server that:
handles customer logins (id only, no password),
accepts messages from clients and distributes them to logged-in clients,
handles the logging out of clients,
collects all responses to client requests in a log, implemented in internal memory (outside the file system).

These tasks are performed by the ChatServer class, which has:
constructor: public ChatServer(String host, int port)
method: public void startServer(), which starts the server in a separate thread,
method: public void stopServer(), which stops the server and the thread it is running in,
 String getServerLog() method - which returns the server log (the required log format will be seen in further examples).

Design requirements for the ChatServer class:
multiplexing of socket channels (use of selector),
the server can handle multiple clients in parallel, but handling client requests in a single thread,

Also provide a ChatClient class with a constructor:

 public ChatClient(String host, int port, String id), gdzie id - id klienta

and the following methods:
public void login() - logs the client onto the server
public void logout() - logs the client off,
public void send(String req) - sends a req request to the server
public String getChatView() - returns the current chat view from the given client's position (i.e. all the information it receives from the server in turn)
For the send method, the request can be sending the text of the message, logging in, logging out, and you can come up with your own communication protocol with the server.

Structural requirements for the ChatClient class
non-blocking input - output

Additionally, create a ChatClientTask class that allows clients to be launched in separate threads via ExecutorService.
Objects of this class are created by a static method:

     public static ChatClientTask create(Client c, List<String> msgs, int wait)

where:
c - client (Client class object)
msgs - list of messages to be sent by the client c
wait - pause time between sending requests.

The code running in the thread should perform the following actions:
connects to the server and logs in (c.login()
sends subsequent messages from the msgs list (c.send(...))
logs out the client (c.logout())

The wait parameter in the create method signature means the time in milliseconds for which the given client's thread is paused after each request. If wait is 0, the client's thread is not paused,

Here is the pseudocode of the fragment responsible for sending requests:

      c.login();
      if (wait != 0) uśpienie_watku_na wait ms;
      // ....
        dla_każdej_wiadomości_z_listy msgs {
          // ...
          c.send( wiadomość );
          if (wait != 0) uśpienie_watku_na wait ms;
        }
      c.logout();
      if (wait != 0) uśpienie_watku_na wait ms;

The project contains a Main class (unmodifiable file), in which information about the server configuration (host, port) and clients (id, thread pause time after each request, set of messages to send) is entered from the test file.

Test file format:
first line: host_name
second line: port number
subsequent lines:
client_id<TAB>wait parameter in ms<TAB>msg1<TAB>msg2<TAB> .... <TAB>msgN

Main class:

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
 
  public static void main(String[] args) throws Exception {

    String testFileName = System.getProperty("user.home") + "/ChatTest.txt";
    List<String> test = Files.readAllLines(Paths.get(testFileName));
    String host = test.remove(0);
    int port = Integer.valueOf(test.remove(0));
    ChatServer s = new ChatServer(host, port);
    s.startServer();
   
    ExecutorService es = Executors.newCachedThreadPool();
    List<ChatClientTask> ctasks = new ArrayList<>();
   
    for (String line : test) {
      String[] elts = line.split("\t");
      String id = elts[0];
      int wait = Integer.valueOf(elts[1]);
      List<String> msgs = new ArrayList<>();
      for (int i = 2; i < elts.length; i++) msgs.add(elts[i] + ", mówię ja, " +id);
      ChatClient c = new ChatClient(host, port, id);
      ChatClientTask ctask = ChatClientTask.create(c, msgs, wait);
      ctasks.add(ctask);
      es.execute(ctask);
    }
    ctasks.forEach( task -> {
      try {
        task.get();
      } catch (InterruptedException | ExecutionException exc) {
        System.out.println("*** " + exc);
      }
    });
    es.shutdown();
    s.stopServer();
   
    System.out.println("\n=== Server log ===");
    System.out.println(s.getServerLog());

    ctasks.forEach(t -> System.out.println(t.getClient().getChatView())); 
  }
}


For a test file of:
localhost
9999
Asia    50    Dzień dobry    aaaa    bbbb    Do widzenia
Adam    50    Dzień dobry    aaaa    bbbb    Do widzenia
Sara    50    Dzień dobry    aaaa    bbbb    Do widzenia



this programme can derive:

Server started

Server stopped

=== Server log ===
00:25:08.698 Asia logged in
00:25:08.698 Sara logged in
00:25:08.745 Adam logged in
00:25:08.745 Sara: Dzień dobry, mówię ja, Sara
00:25:08.745 Asia: Dzień dobry, mówię ja, Asia
00:25:08.807 Sara: aaaa, mówię ja, Sara
00:25:08.807 Adam: Dzień dobry, mówię ja, Adam
00:25:08.807 Asia: aaaa, mówię ja, Asia
00:25:08.869 Adam: aaaa, mówię ja, Adam
00:25:08.869 Sara: bbbb, mówię ja, Sara
00:25:08.869 Asia: bbbb, mówię ja, Asia
00:25:08.932 Adam: bbbb, mówię ja, Adam
00:25:08.932 Sara: Do widzenia, mówię ja, Sara
00:25:08.932 Asia: Do widzenia, mówię ja, Asia
00:25:08.994 Sara logged out
00:25:08.994 Adam: Do widzenia, mówię ja, Adam
00:25:08.994 Asia logged out
00:25:09.057 Adam logged out

=== Asia chat view
Asia logged in
Sara logged in
Adam logged in
Sara: Dzień dobry, mówię ja, Sara
Asia: Dzień dobry, mówię ja, Asia
Sara: aaaa, mówię ja, Sara
Adam: Dzień dobry, mówię ja, Adam
Asia: aaaa, mówię ja, Asia
Adam: aaaa, mówię ja, Adam
Sara: bbbb, mówię ja, Sara
Asia: bbbb, mówię ja, Asia
Adam: bbbb, mówię ja, Adam
Sara: Do widzenia, mówię ja, Sara
Asia: Do widzenia, mówię ja, Asia
Sara logged out
Adam: Do widzenia, mówię ja, Adam
Asia logged out

=== Adam chat view
Adam logged in
Sara: Dzień dobry, mówię ja, Sara
Asia: Dzień dobry, mówię ja, Asia
Sara: aaaa, mówię ja, Sara
Adam: Dzień dobry, mówię ja, Adam
Asia: aaaa, mówię ja, Asia
Adam: aaaa, mówię ja, Adam
Sara: bbbb, mówię ja, Sara
Asia: bbbb, mówię ja, Asia
Adam: bbbb, mówię ja, Adam
Sara: Do widzenia, mówię ja, Sara
Asia: Do widzenia, mówię ja, Asia
Sara logged out
Adam: Do widzenia, mówię ja, Adam
Asia logged out
Adam logged out


The example shows the required form of printing (client chatView, server log entries). In particular:
starting the server results in the following message being printed on the console: Server started
logging in the client id results in the following message being sent: id logged in
logging out the client id results in the following message being sent: id logged out
receiving a msg message from client id results in the following message being sent: msg
the chat view returned by client.getChatView() for client id is preceded by the header: === id chat view
the server log is preceded by the header === Server log === and contains all server responses in turn with the time in the format HH:MM:SS.nnn, where nnn - milliseconds (time according to the system clock),
stopping the server prints out the following message on the console: Server stopped
any errors in the client's interaction with the server (exc exceptions, e.g. IOException) should be added to the client's chatView as exc.toString() preceded by three asterisks
The printout form is mandatory, and failure to do so results in the loss of points. The specific content of the chatview and seer log printouts (line order, times, etc.) may be different in each run, but it is important to see parallel client service and the logic preserved: clients receive, in the correct order, only those messages that appeared from the moment they logged in to the moment they logged out.

Summary:
you need to create the ChatServer, ChatClient, ChatClientTask classes in such a way as to ensure proper execution of the code of the main method from the Main class

But
you need to prepare the code for the numerous configurations provided in the ChatTest.txt file.

Examples of Main.main() results:
Dla:
localhost
33333
Asia    20    Dzień dobry    beee    Do widzenia
Sara    20    Dzień dobry    muuu    Do widzenia

Wynik:
Server started

Server stopped

=== Server log ===
01:18:43.723 Asia logged in
01:18:43.723 Sara logged in
01:18:43.738 Asia: Dzień dobry, mówię ja, Asia
01:18:43.738 Sara: Dzień dobry, mówię ja, Sara
01:18:43.769 Sara: muuu, mówię ja, Sara
01:18:43.769 Asia: beee, mówię ja, Asia
01:18:43.801 Asia: Do widzenia, mówię ja, Asia
01:18:43.801 Sara: Do widzenia, mówię ja, Sara
01:18:43.832 Asia logged out
01:18:43.832 Sara logged out

=== Asia chat view
Asia logged in
Sara logged in
Asia: Dzień dobry, mówię ja, Asia
Sara: Dzień dobry, mówię ja, Sara
Sara: muuu, mówię ja, Sara
Asia: beee, mówię ja, Asia
Asia: Do widzenia, mówię ja, Asia
Sara: Do widzenia, mówię ja, Sara
Asia logged out

=== Sara chat view
Sara logged in
Asia: Dzień dobry, mówię ja, Asia
Sara: Dzień dobry, mówię ja, Sara
Sara: muuu, mówię ja, Sara
Asia: beee, mówię ja, Asia
Asia: Do widzenia, mówię ja, Asia
Sara: Do widzenia, mówię ja, Sara
Asia logged out
Sara logged out


Dla:
localhost
55557
Asia    10    Dzień dobry    beee    Do widzenia
Sara    20    Dzień dobry    muuu    Do widzenia

Server started

Server stopped

=== Server log ===
01:25:33.293 Asia logged in
01:25:33.293 Asia: Dzień dobry, mówię ja, Asia
01:25:33.308 Asia: beee, mówię ja, Asia
01:25:33.324 Sara logged in
01:25:33.324 Asia: Do widzenia, mówię ja, Asia
01:25:33.339 Asia logged out
01:25:33.355 Sara: Dzień dobry, mówię ja, Sara
01:25:33.386 Sara: muuu, mówię ja, Sara
01:25:33.417 Sara: Do widzenia, mówię ja, Sara
01:25:33.449 Sara logged out

=== Asia chat view
Asia logged in
Asia: Dzień dobry, mówię ja, Asia
Asia: beee, mówię ja, Asia
Sara logged in
Asia: Do widzenia, mówię ja, Asia
Asia logged out

=== Sara chat view
Sara logged in
Asia: Do widzenia, mówię ja, Asia
Asia logged out
Sara: Dzień dobry, mówię ja, Sara
Sara: muuu, mówię ja, Sara
Sara: Do widzenia, mówię ja, Sara
Sara logged out

A dla takiej konfiguracji (zagłodzenie wątku):
localhost
55557
Asia    0    Dzień dobry    beee    Do widzenia
Sara    0    Dzień dobry    muuu    Do widzenia

Wynik:
Server started

Server stopped

=== Server log ===
01:50:13.603 Asia logged in
01:50:13.603 Asia: Dzień dobry, mówię ja, Asia
01:50:13.603 Asia: beee, mówię ja, Asia
01:50:13.603 Asia: Do widzenia, mówię ja, Asia
01:50:13.603 Asia logged out
01:50:13.634 Sara logged in
01:50:13.634 Sara: Dzień dobry, mówię ja, Sara
01:50:13.634 Sara: muuu, mówię ja, Sara
01:50:13.634 Sara: Do widzenia, mówię ja, Sara
01:50:13.634 Sara logged out

=== Asia chat view
Asia logged in
Asia: Dzień dobry, mówię ja, Asia
Asia: beee, mówię ja, Asia
Asia: Do widzenia, mówię ja, Asia
Asia logged out

=== Sara chat view
Sara logged in
Sara: Dzień dobry, mówię ja, Sara
Sara: muuu, mówię ja, Sara
Sara: Do widzenia, mówię ja, Sara
Sara logged out



Note: The Main.java file is unmodifiable.
