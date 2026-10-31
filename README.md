# JavaFX Maze Generator & Solver

A modern, feature-rich desktop application built with **JavaFX** for generating, solving, and playing custom labyrinths. It features a sleek, cohesive interface with live theme switching, interactive gameplay, and multiple maze generation algorithms.

## Features

- **Multiple Generation Algorithms**: 
  - *Recursive Backtracker (DFS)*
  - *Randomized Prim's Algorithm*
  - *Randomized Kruskal's Algorithm*
- **Interactive Playable Mode**: Test your puzzle-solving skills by navigating the maze using keyboard controls (`WASD`) with live step-counting and a celebratory victory popup.
- **Visual Path Solver**: Instantly compute and display the shortest path using a Breadth-First Search (BFS) algorithm.
- **Dynamic Theme Switcher**: Seamlessly toggle between a sleek modern **Dark Mode** and a clean **Light Mode** (adapting both the UI controls and the maze board canvas).
- **Customizable Configuration**: Live-updating, integer-snapping sliders for maze width, height, and loop/braid factor.
- **Real-time Statistics**: Track metrics like total cell count, dead ends, and solution path length.
- **Image Export**: Export your generated maze directly as a high-resolution PNG image.


## Getting Started

### For Users (Running the Release)
1. Download the latest release `.zip` package from the [Releases](https://github.com/GreyTimes/Maze-Generator/releases) page.
2. Extract the archive anywhere on your computer.
3. Open the extracted folder, navigate to the **`bin`** directory.
4. Double-click **`mazeGenerator.bat`** (Windows) to launch the application.

#### User Guide
1. Configure Your Maze:
- Use the Algorithm dropdown to choose your preferred generation strategy.
- Adjust the Width (Cols), Height (Rows), and Loop/Braid Factor (%) sliders to configure your layout. Changes reflect live on screen!

2. Generate: Click "Generate Labyrinth" to watch the maze build in real-time.

3. Solve: Click "Show Solution" at any time to compute and highlight the optimal shortest path via BFS.

4. Play Mode:
- Toggle "Playable Mode: ON".
- Use your keyboard's Arrow Keys or WASD to navigate your way from the entrance on the left to the exit on the right.

5. Export: Click "Export Image" to save your completed maze as a high-quality .png file.

5. Themes: Toggle the Theme button to switch instantly between Dark and Light appearances.

### For Developers(Running via Gradle)
### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher (Java 21+ recommended).
- **Gradle**: Included via Gradle Wrapper (`gradlew`).

#### Clone the repository
```
git clone [https://github.com/YourUsername/mazeGenerator.git](https://github.com/YourUsername/mazeGenerator.git)
cd mazeGenerator
```
#### Run the application (Windows)
```
gradlew run
```
#### Run the application (macOS / Linux)
```
./gradlew run
```

### Built With
- JavaFX - GUI framework
- Gradle - Build automation and dependency management

### License
This project is open-source and available under the MIT License.
