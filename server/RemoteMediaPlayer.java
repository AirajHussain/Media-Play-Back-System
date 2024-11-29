import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;



public interface RemoteMediaPlayer extends Remote {

    // Identifier for the RMI registry to locate the MediaControlService
    String SERVICE_IDENTIFIER = "MediaControlService";

    // Starts playback of the specified track on the given output device
    void startPlayback(String trackName, String outputDevice) throws RemoteException;

    // Retrieves a list of all tracks available on the server
    List<String> listAllTracks() throws RemoteException;

    // Fetches a list of all playback devices available for use
    List<String> fetchAvailableDevices() throws RemoteException;

    // Uploads a track to the server using the provided name and binary data
    String uploadTrack(String trackName, byte[] trackData) throws RemoteException;

    // Pauses specified track
    void pauseTrack(String trackName) throws RemoteException;

    // Resumes specified track 
    void resumeTrack(String trackName) throws RemoteException;

    // Stops playback of specified track 
    void stopPlayback(String trackName) throws RemoteException;

    // Adjusts the volume of the specified track 
    String adjustVolume(String trackName, boolean increase) throws RemoteException;
}
