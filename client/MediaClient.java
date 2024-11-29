// Group 22 Distributed Final Project
// Syed Airaj Hussain, Mohammed Adnan Hashmi, Nathan Yohannes and Rupam Mittal 

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MediaClient {

    public static void main(String[] args) {
        try {

            // Connecting remote media player to service 
            RemoteMediaPlayer mediaController = (RemoteMediaPlayer) Naming.lookup(RemoteMediaPlayer.SERVICE_IDENTIFIER);
            Scanner inputScanner = new Scanner(System.in);

            // Display the main menu to the user
            System.out.println("-----------WELCOME to the Media Playback System-----------");
            System.out.println("1. Select a song to play");
            System.out.println("2. Upload a new song\n");
            System.out.print("Enter Here:");
            String userChoice = inputScanner.nextLine();

            // Handle user choice
            if (userChoice.equals("1")) {
                selectDeviceAndTrack(mediaController);
            } else if (userChoice.equals("2")) {
                uploadTrack(mediaController);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // Method to handle device and track selection
    static void selectDeviceAndTrack(RemoteMediaPlayer mediaController) throws RemoteException {
        try {


            System.out.println("\n"); 
            System.out.println("Select a audio device:");
            Scanner inputScanner = new Scanner(System.in);

            // Retrieve and ensure unique playback devices
            List<String> uniqueDevices = new ArrayList<>();
            for (String device : mediaController.fetchAvailableDevices()) {
                if (!uniqueDevices.contains(device)) {
                    uniqueDevices.add(device);
                }
            }

            // Display devices starting from index 1-
            for (int index = 0; index < uniqueDevices.size(); index++) {
                System.out.println((index + 1) + " - " + uniqueDevices.get(index));
            }
            System.out.print("Enter Here: ");

            // Allow user to select a device
            String selectedDeviceIndex = inputScanner.nextLine();
            int deviceIndex = Integer.parseInt(selectedDeviceIndex) - 1; // Adjust to 0-based indexing
            if (deviceIndex < 0 || deviceIndex >= uniqueDevices.size()) {
                System.out.println("Invalid device selection.");
                return;
            }

            System.out.println("\n"); 
            String selectedDevice = uniqueDevices.get(deviceIndex);
            System.out.println("Selected audio device: " + selectedDevice);
    
            // Display tracks to the user
            System.out.println("Select a track to play:");
            List<String> allTracks = mediaController.listAllTracks();

            // Display tracks starting from index 1
            for (int index = 0; index < allTracks.size(); index++) {
                System.out.println((index + 1) + " - " + allTracks.get(index));
            }
            System.out.print("Enter Here: ");

            // Allow user to select a track
            String selectedTrackIndex = inputScanner.nextLine();
            int trackIndex = Integer.parseInt(selectedTrackIndex) - 1; // Adjust to 0-based indexing
            if (trackIndex < 0 || trackIndex >= allTracks.size()) {
                System.out.println("Invalid track selection.");
                return;
            }
            String selectedTrack = allTracks.get(trackIndex);
            System.out.println("Selected track: " + selectedTrack);

            System.out.println("\n");
    
            // Start playback for the selected track and device
            mediaController.startPlayback(selectedTrack, selectedDevice);
    
            // Options menu for controlling playback
            while (true) {
                System.out.println("\n---------- AUDIO OPTIONS ---------- \n 'p' for pause\n 'c' to continue \n '+' increasing volume \n '-' decreasing volume \n 'n' for a new device \n 'e' to exit\n");
                System.out.print("Enter Here: ");
                
                // Handle user input for playback options
                switch (inputScanner.nextLine()) {
                    case "p":
                        mediaController.pauseTrack(selectedTrack);
                        break;
                    case "c":
                        mediaController.resumeTrack(selectedTrack);
                        break;
                    case "+":
                        mediaController.adjustVolume(selectedTrack, true);
                        break;
                    case "-":
                        mediaController.adjustVolume(selectedTrack, false);
                        break;
                    case "u":
                        uploadTrack(mediaController);
                        break;
                    case "n":
                        System.out.println("Audio has stopped . . .");
                        mediaController.stopPlayback(selectedTrack);
                        selectDeviceAndTrack(mediaController);
                        break;
                    case "e":
                        mediaController.stopPlayback(selectedTrack);
                        System.out.println("Leaving the program. ");
                        System.exit(0);
                    default:
                        System.out.println("Invalid option, try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Helper method to load track data from a file
    private static byte[] loadTrackData(String filePath) throws IOException {
        File f = new File(filePath);
        return Files.readAllBytes(f.toPath());
    }

    // Method to upload a new track to the server
    static void uploadTrack(RemoteMediaPlayer mediaController) {
        Scanner inputScanner = new Scanner(System.in);
        String directoryPath = "media_tracks";
        File trackDirectory = new File(directoryPath);
        List<String> availableTracks = new ArrayList<>();
    
        try {
            // Display tracks available for upload
            System.out.println("\nSelect a track to upload:");
       
            int counter = 1; // Start numbering from 1
            if (trackDirectory.exists() && trackDirectory.isDirectory()) {
                File[] files = trackDirectory.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            System.out.println(counter + " - " + f.getName());
                            availableTracks.add(f.getName());
                            counter++;
                        }
                    }
                } else {
                    System.err.println("Error accessing directory contents.");
                }
            } else {
                System.err.println("Invalid directory path.");
            }
            System.out.print("Enter Here:");
    
            // Allow user to select a track for upload
            String trackToUploadIndex = inputScanner.nextLine();
            int trackIndex = Integer.parseInt(trackToUploadIndex) - 1; // Adjust user input to 0-based index
            String trackToUpload = availableTracks.get(trackIndex);
            byte[] trackData = loadTrackData(directoryPath + "/" + trackToUpload);
    
            // Upload the track to the server
            System.out.println(mediaController.uploadTrack(trackToUpload, trackData));
            selectDeviceAndTrack(mediaController);
    
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
