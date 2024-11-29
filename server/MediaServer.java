import java.rmi.Naming;

public class MediaServer {
    public static void main(String[] args) throws Exception {

        //Instance creation 
        RemoteMediaPlayerImpl mediaController = new RemoteMediaPlayerImpl();

        //Instance binding to RMIREGISTRY 
        Naming.rebind(RemoteMediaPlayer.SERVICE_IDENTIFIER, mediaController);

        System.out.println("The Media Server is up and running!");
    }
}
