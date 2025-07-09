# Prisma Graph Visualizer Plugin

A JetBrains IntelliJ IDEA / WebStorm / PHPStorm plugin for graphical visualization of Prisma Schema files.

## 🚀 Features

- **Automatic Detection**: Automatically detects all `.prisma` files in your project
- **Graphical Visualization**: Displays models, fields, and relationships in an interactive graph
- **Live Updates**: Updates automatically when schema files change
- **Interactive Navigation**: 
  - Zoom with mouse wheel
  - Pan with right-click + drag
  - Model selection by clicking
  - Model movement by dragging
- **Multiple Schema Files**: Supports multiple schema files and combines them into a single graph
- **Relationship Visualization**: Shows One-to-One, One-to-Many, and Many-to-Many relationships
- **Custom Tool Window**: Dedicated tool window for graph visualization

## 📋 Requirements

- **JetBrains IDE**: IntelliJ IDEA, WebStorm, or PHPStorm 2025.1.3 or higher
- **Java**: JDK 17 or higher
- **Build System**: Gradle 7.0+ (or use included Gradle Wrapper)

## 🔧 Installation

### Option 1: Build from Source
```bash
# Clone the repository
git clone https://github.com/AlexanderKoch-UX/prisma-graph.git
cd prisma-graph

# Build the plugin
./gradlew build

# Install the plugin
./gradlew buildPlugin
```

### Option 2: IDE Plugin Installation
1. Open your JetBrains IDE
2. Go to **File** → **Settings** → **Plugins**
3. Click **Install Plugin from Disk**
4. Select the built plugin ZIP file from `build/distributions/`

## 🛠️ Development

### Build Commands
```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Run IDE with plugin for testing
./gradlew runIde

# Build plugin distribution
./gradlew buildPlugin
```

### Technical Details
- **Language**: Kotlin 2.0.21
- **Target JVM**: Java 17
- **IDE Platform**: IntelliJ Platform 2025.1.3
- **Build System**: Gradle with IntelliJ Platform Plugin

## 📖 Usage

1. Open a project containing `.prisma` files
2. Open the **Prisma Graph** tool window (View → Tool Windows → Prisma Graph)
3. The graph will automatically display your schema structure
4. Use mouse interactions to navigate and explore your data model

## 🔄 Version History

### Version 1.0.0
- ✅ Compatibility with IntelliJ IDEA 2025.1.3 and higher (Build 251.*)
- ✅ Initial release of Prisma Graph Visualizer
- ✅ Basic schema parsing functionality
- ✅ Graphical display of models and relationships
- ✅ Interactive graph navigation
- ✅ Live schema updates

## 🧪 Testing

Run the test suite with:
```bash
./gradlew test
```

## 📝 License

MIT License