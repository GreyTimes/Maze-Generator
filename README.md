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
1. Download the latest release `.zip` package from the [Releases](https://github.com/GreyTimes/mazeGenerator/releases) page.
2. Extract the archive anywhere on your computer.
3. Open the extracted folder, navigate to the **`bin`** directory.
4. Double-click **`mazeGenerator.bat`** (Windows) to launch the application.

### For Developers(Running via Gradle)
### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher (Java 21+ recommended).
- **Gradle**: Included via Gradle Wrapper (`gradlew`).
---
- Clone the repository
```
git clone [https://github.com/YourUsername/mazeGenerator.git](https://github.com/YourUsername/mazeGenerator.git)
cd mazeGenerator
```
- Run the application (Windows)
```
gradlew run
```
- Run the application (macOS / Linux)
```
./gradlew run
```

### Built With
- JavaFX - GUI framework
- Gradle - Build automation and dependency management

### License
This project is open-source and available under the MIT License.
