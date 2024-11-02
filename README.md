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

-Java Development Kit (JDK) 11 or higher.

-JavaFX SDK

### Step 1: Download and Set Up JavaFX SDK if it's not already installed

If you’re using JDK 11 or higher, [download the JavaFX SDK](https://gluonhq.com/products/javafx/).

- Extract the SDK to a location on your system, for example:
    ```
    C:\path\to\javafx-sdk
    ```
- To verify installation, run the following command:
  ```
  javac --version
  ```
  NOTE: If there are compatibility errors between the sdk and jdk, try downloading an older sdk version such as 17.03.10
  
### Step 2: Make sure you are running version Java 11 or higher

-Run the following command to verify your version
```
java --version
```
-If needed, you can download the most recent version of Java here: [download the JDK](https://www.oracle.com/java/technologies/downloads/#jdk23-linux).

### Step 3: Clone the Repository
1. Open your terminal (or command prompt) and clone the repository:
   ```
   git clone https://github.com/wgolden117/connect4_local_gameplay.git
   ```
2. Navigate to the project directory
   ```
   cd connect4_local_gameplay
   ```
3. Compile the program:
   ```
   javac --module-path "\path\to\javafx-sdk-22\lib" --add-modules javafx.controls,javafx.fxml -d bin src/module-info.java src/core/*.java src/ui/*.java
   ```

4. Run the program:
   - Option 1:
    ```
    java --module-path "\path\to\javafx-sdk-22\lib" --add-modules javafx.controls,javafx.fxml -cp bin ui.GUI
    ```

   - Option 2:
    ```
     java --module-path "\path\to\javafx-sdk-22\lib" --add-modules javafx.controls,javafx.fxml -cp bin ui.ConsoleUI
    ```

   -Replace ""\path\to\javafx-sdk-22\lib" with your path to the lib folder of the SDK
   
   -For example: "C:\Users\Weronika Golden\Downloads\openjfx-22_windows-x64_bin-sdk\javafx-sdk-22\lib"

### Step 4: Running in an IDE (IntelliJ, Eclipse)

1. Set up your IDE:

   - Open the project in your favorite IDE (e.g., IntelliJ or Eclipse).
   - Add the JavaFX SDK to the project:
     - **In IntelliJ**: Go to File > Project Structure > Libraries, click `+` to add the JavaFX SDK.
     - **In Eclipse**: Go to Project > Properties > Java Build Path, and add the JavaFX SDK as an external library.

2. Edit VM Options:

   - In your IDE's run configuration, add the following VM options to run the JavaFX-based GUI:
     ```bash
     --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml
     ```
   - Replace `"C:\path\to\javafx-sdk\lib"` with the actual path to your JavaFX SDK.

3. Run the Application:

   - Select either `ConsoleUI` or `GUI` and run it from the IDE.

Credits
Developed by Weronika Golden as part of a local Connect4 game project.
