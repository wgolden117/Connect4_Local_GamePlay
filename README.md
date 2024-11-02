# Connect4 Local Gameplay
This is a Java implementation of the Connect4 game, which can be played either through a Console interface or a Graphical User Interface (GUI) using JavaFX. 
The game allows users to play against another player or the computer locally on their machine.

## Description
The project includes:
-A console-based version of the game (ConsoleUI).
-A JavaFX GUI-based version (GUI).
-Game logic handled by classes in the core package.

## Prerequisites
To run this project, you need:
-Java Development Kit (JDK) 11.
-JavaFX SDK.

## Installation
### Step 1: Clone the Repository
1. Open your terminal (or command prompt) and clone the repository:
   ```
   git clone https://github.com/wgolden117/connect4_local_gameplay.git
   ```
   OR

- Click on the <> Code button with the drop down arrow and download the zip file
- Extract the zip file

### Step 2: Download and Set Up JavaFX SDK
If you’re using JDK 11 or higher, [download the JavaFX SDK](https://gluonhq.com/products/javafx/).
NOTE: Make sure to download version 17.0.13 for your system, other versions are not yet compatible

- Extract the SDK to a location on your system, for example:
    ```
    C:\path\to\javafx-sdk
    ```
### Step 3: Compile the Program
#### Command Line (Windows/macOS/Linux)
1. Navigate into the source folder of the project directory:
    -Here is an example of what that would look like
   ```
   cd "C:\Users\Weronika Golden\Downloads\Connect4_Local_GamePlay-master\Connect4_Local_GamePlay-master\src"
   ```
   -Replace the path with your own path to the src file
   
3. Compile the project:

    ```bash
    javac --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml ui/*.java core/*.java
    ```

   -Replace `C:\path\to\javafx-sdk\lib` with the actual path where you extracted the JavaFX SDK.

### Step 4: Running the Game
#### Use the following command:
```bash
java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml ui.ConsoleUI
```
 -Replace `C:\path\to\javafx-sdk\lib` with the actual path where you extracted the JavaFX SDK.

### Step 5: Running in an IDE (IntelliJ, Eclipse)
1. Set up your IDE:
Open the project in your favorite IDE (e.g., IntelliJ or Eclipse).
Add the JavaFX SDK to the project:
In IntelliJ: Go to File > Project Structure > Libraries, click + to add the JavaFX SDK.
In Eclipse: Go to Project > Properties > Java Build Path, and add the JavaFX SDK as an external library.
2. Edit VM Options:

In your IDE's run configuration, add the following VM options to run the JavaFX-based GUI:
--module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml
Replace "C:\path\to\javafx-sdk\lib" with the actual path to your JavaFX SDK.

Run the Application:
Select either ConsoleUI or GUI and run it from the IDE.

Credits
Developed by Weronika Golden as part of a local Connect4 game project.
