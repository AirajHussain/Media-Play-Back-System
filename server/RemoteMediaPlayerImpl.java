import javax.sound.sampled.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RemoteMediaPlayerImpl extends UnicastRemoteObject implements RemoteMediaPlayer {

    
    private class PlaybackTask extends Thread {
        private final AudioInputStream audioStream; 
        private final SourceDataLine audioLine; 
        private volatile boolean isRunning = true; 
        private boolean isPaused = false; 

        public PlaybackTask(AudioInputStream audioStream, SourceDataLine audioLine) {
            this.audioStream = audioStream;
            this.audioLine = audioLine;
        }

        // Pausing track
        public void togglePause(boolean pauseState) {
            isPaused = pauseState;
        }

        // Stopping track
        public void stopTask() {

            //Signal thread to stop, and interrupting it if its sleeping
            isRunning = false; 
            interrupt(); 
        }

        @Override
        public void run() {
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;

                // Loop for playback while the thread is running
                while (isRunning && (bytesRead = audioStream.read(buffer)) != -1) {
                    if (isPaused) {
                        Thread.sleep(10); 
                        continue;
                    }
                    audioLine.write(buffer, 0, bytesRead); 
                }
            } catch (IOException e) {
                System.err.println("Error during playback: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Playback interrupted.");
            } finally {
                
                try {
                    audioLine.drain();
                    audioLine.stop();
                    audioLine.close();
                    audioStream.close();

                } catch (IOException e) {
                    System.err.println("Error closing resources: " + e.getMessage());
                }
            }
        }
    }

    // Storing tracks 
    private final Map<String, PlaybackTask> playbackTasks = new HashMap<>();

    
    protected RemoteMediaPlayerImpl() throws RemoteException {
        super();
    }

    // Starts task on user chosen device 
    @Override
    public void startPlayback(String trackName, String deviceName) throws RemoteException {
        try {
            String trackPath = "media_tracks/" + trackName;
            File audioFile = new File(trackPath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);

            // Locate the specified device
            Mixer.Info selectedDevice = null;
            for (Mixer.Info device : AudioSystem.getMixerInfo()) {
                if (device.getName().equals(deviceName)) {
                    selectedDevice = device;
                    break;
                }
            }

            if (selectedDevice == null) {
                System.out.println("Device not found: " + deviceName);
                return;
            }

            // Open and start the audio line
            Mixer mixer = AudioSystem.getMixer(selectedDevice);
            AudioFormat format = stream.getFormat();
            DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) mixer.getLine(lineInfo);
            line.open(format);
            line.start();

            // Create and start a new playback task
            PlaybackTask playbackTask = new PlaybackTask(stream, line);
            playbackTasks.put(trackName, playbackTask);
            playbackTask.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // All tracks in Media_Tracks dir displaying 
    @Override
    public List<String> listAllTracks() throws RemoteException {
        String trackDirectory = "media_tracks";
        File directory = new File(trackDirectory);
        List<String> tracks = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        tracks.add(file.getName());
                    }
                }
            }
        }

        return tracks;
    }

    // Getting all devices located, via bluetooth as well 
    @Override
    public List<String> fetchAvailableDevices() throws RemoteException {
        List<String> playbackDevices = new ArrayList<>();
        Mixer.Info[] audioMixerInfoArray = AudioSystem.getMixerInfo();
        for (Mixer.Info audioMixerInfo : audioMixerInfoArray) {

            Mixer mixer = AudioSystem.getMixer(audioMixerInfo);
            Line.Info[] availableLineInfoArray = mixer.getSourceLineInfo();

            for (Line.Info audioLineInfo : availableLineInfoArray) {
                if (audioLineInfo.getLineClass().equals(SourceDataLine.class)) {
                    String playbackDeviceName = audioMixerInfo.getName();
                    if (!playbackDevices.contains(playbackDeviceName)) {
                        playbackDevices.add(playbackDeviceName);
                    }
                    break; 
                }
            }
            
        }
        return playbackDevices;
    }

    // Uploads a new track to the server
    @Override
    public String uploadTrack(String trackName, byte[] trackData) throws RemoteException {
        try {
            String trackPath = "media_tracks/" + trackName;
            FileOutputStream outputStream = new FileOutputStream(trackPath);
            outputStream.write(trackData);
            outputStream.close();
            return "Track uploaded: " + trackPath;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload track.";
        }
    }

    // Pauses playback for the specified track
    @Override
    public void pauseTrack(String trackName) throws RemoteException {
        PlaybackTask task = playbackTasks.get(trackName);
        if (task != null) {
            task.togglePause(true);
        }
    }

    // Resumes playback for the specified track
    @Override
    public void resumeTrack(String trackName) throws RemoteException {
        PlaybackTask task = playbackTasks.get(trackName);
        if (task != null) {
            task.togglePause(false);
        }
    }

    // Stops playback for the specified track
    @Override
    public void stopPlayback(String trackName) throws RemoteException {
        PlaybackTask task = playbackTasks.get(trackName);
        if (task != null) {
            task.stopTask(); // Signal the playback task to stop
            playbackTasks.remove(trackName);
            System.out.println("Playback stopped for track: " + trackName);
        } else {
            System.out.println("No playback found for track: " + trackName);
        }
    }

    // Adjusts the volume of the specified track
    @Override
    public String adjustVolume(String trackName, boolean increase) throws RemoteException {
        PlaybackTask task = playbackTasks.get(trackName);

        if (task != null) {
            try {
                FloatControl volumeControl = (FloatControl) task.audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                float currentVolume = volumeControl.getValue();
                float newVolume = increase ? currentVolume + 5.0f : currentVolume - 5.0f;
                newVolume = Math.max(volumeControl.getMinimum(), Math.min(volumeControl.getMaximum(), newVolume));
                volumeControl.setValue(newVolume);
                return "Volume adjusted to: " + newVolume;
            } catch (Exception e) {
                return "Volume control error.";
            }
        }

        return "Track not found: " + trackName;
    }
}
