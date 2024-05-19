/**
 *
 *  @author Kurzau Kiryl S24911
 *
 */

package zad1;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<ChatClient> {
    public ChatClientTask(Callable<ChatClient> callable) {
        super(callable);
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(()-> {
            c.login();
            if (wait!=0){
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            msgs.forEach( (req) ->{
                c.send(req);
                if (wait!=0){
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            c.logout();
            if (wait!=0){
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return c;
        });
    }

    public ChatClient getClient() {
        try {
            return this.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
