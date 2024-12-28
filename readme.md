# Media Play Back System 
(Final Project Distributed System Instructions) 

## Compilation

### Server
1. Navigate to the server directory:
   ```bash
   cd /(folder_name)/server
   ```
2. Compile all server-side Java files:
   ```bash
   javac *.java
   ```

### Client
1. Navigate to the client directory:
   ```bash
   cd /(folder_name)/client
   ```
2. Compile all client-side Java files:
   ```bash
   javac *.java
   ```

---

## Running the Application

### Step 1: Start `rmiregistry`
1. Navigate to the server directory:
   ```bash
   cd /(folder_name)/server
   ```
2. Start the `rmiregistry` on your preferred port (default: `1099`):
   ```bash
   rmiregistry 1099
   ```

### Step 2: Start the Server
1. Open a new terminal.
2. Navigate to the server directory:
   ```bash
   cd /(folder_name)/server
   ```
3. Run the server:
   ```bash
   java MediaServer
   ```

### Step 3: Start the Client
1. Open another terminal.
2. Navigate to the client directory:
   ```bash
   cd /(folder_name)/client
   ```
3. Run the client:
   ```bash
   java MediaClient
   ```

---

## Terminal Setup
You should have **three separate terminals**:
1. One for the `rmiregistry`.
2. One for the server (`MediaServer`).
3. One for the client (`MediaClient`).

---

## Client Instructions
Once the client is running, follow the prompts displayed in the client terminal. Make your selections based on the options provided, and the client will guide you through the functionality (e.g., playback, uploading tracks, or listing available devices).


