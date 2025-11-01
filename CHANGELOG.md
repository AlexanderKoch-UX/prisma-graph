# Changelog - Prisma Graph Visualizer

All notable changes to the Prisma Graph Visualizer plugin are documented in this file.

## [1.0.1] - 2025-11-01

### 🐛 Bug Fixes

#### Fixed Missing Prisma Icons
- **Issue**: Plugin displayed oversized teal blocks instead of proper Prisma icons
- **Affected Areas**: File tabs, project explorer, IDE file lists
- **Solution**: 
  - Created SVG icon files: `prisma.svg`, `prisma_16x16.svg`
  - Updated `PrismaFileType.kt` to load SVG icons with PNG fallback
  - Enhanced `PrismaIconProvider.kt` with improved error handling
  - Added graceful fallback to system icons if custom icons unavailable
- **Files Changed**:
  - `src/main/resources/icons/prisma.svg` (NEW)
  - `src/main/resources/icons/prisma_16x16.svg` (NEW)
  - `src/main/kotlin/com/alexanderkoch/prismagraph/filetype/PrismaFileType.kt`
  - `src/main/kotlin/com/alexanderkoch/prismagraph/icons/PrismaIconProvider.kt`

#### Fixed Mac M1/MacOS Touchpad Zoom Behavior
- **Issue**: Mac trackpad scroll caused extreme zoom and bouncing (too aggressive)
- **Root Cause**: Scale factor (1.1/0.9) was too large for frequent trackpad events
- **Solution**:
  - Reduced zoom scale factor from 1.1x/0.9x to 1.05x/0.95x (50% less aggressive)
  - Added mouse wheel event throttling: 50Hz max (20ms minimum between events)
  - Improved zoom calculation for smoother experience on all platforms
  - Better handling of cursor-focused zoom
- **Impact**: 
  - Smooth, controllable zoom on Mac M1 with trackpad
  - Consistent experience with other Mac applications
  - No more extreme zoom jumps or bouncing behavior
- **Files Changed**:
  - `src/main/kotlin/com/alexanderkoch/prismagraph/ui/PrismaGraphPanel.kt`
    - Added `lastWheelEventTime` and `wheelEventThrottleMs` variables
    - Updated `setupMouseListeners()` mouse wheel listener
    - Added event throttling logic
    - Reduced scale factors for conservative zoom

#### Fixed Model Dragging Boundary Limitation
- **Issue**: Models could not be dragged to upper left corner (hit invisible wall)
- **Root Cause**: Minimum position constraint (50px from origin) prevented corner positioning
- **Solution**:
  - Removed position boundary enforcement in `ensureMinimumDistance()`
  - Removed position boundary enforcement in `ensureMinimumDistanceForDrag()`
  - Enabled models to be freely positioned anywhere on canvas
- **Impact**:
  - Users can now drag models to all edges including upper left corner
  - Full canvas utilization for schema organization
  - No invisible boundaries or constraints
- **Files Changed**:
  - `src/main/kotlin/com/alexanderkoch/prismagraph/ui/PrismaGraphPanel.kt`
    - Updated line 148-150 in `ensureMinimumDistance()`
    - Updated line 186-188 in `ensureMinimumDistanceForDrag()`

### ⚡ Performance Improvements

- Added mouse wheel event throttling to prevent excessive event processing
- Reduced unnecessary repaint operations during rapid zoom events
- Better performance on systems with slow graphics rendering
- Particularly beneficial for Mac M1 systems handling trackpad events

### 📦 Enhancements

- SVG icon format provides better quality at all DPI settings
- Proper icon scaling for high-resolution displays (Retina, 4K)
- Improved fallback behavior if icon resources unavailable
- Better error messages in developer logs

### 📝 Documentation

- Added `RELEASE_NOTES_v1.0.1.md` with detailed release information
- Added `DEPLOYMENT_GUIDE.md` for distribution instructions
- Added `UPDATE_SUMMARY_FOR_CHRIS_BYERS.md` for end-user communication
- Updated version information in `build.gradle.kts`
- Updated change notes in `plugin.xml`

### 🔄 Backward Compatibility

- ✅ Fully backward compatible with v1.0.0
- ✅ No breaking changes to API or configuration
- ✅ Existing projects continue to work without modification
- ✅ Smooth migration path for all users

### 🧪 Testing

- Verified icon loading on Mac M1 WebStorm
- Tested touchpad zoom behavior on Mac trackpad
- Verified model dragging to all corners
- Confirmed compatibility with all supported IDEs
- Tested fallback behavior when resources unavailable

### 📋 Version Bumps

- `version`: 1.0.0 → 1.0.1 (build.gradle.kts)
- `version`: 1.0.0 → 1.0.1 (plugin.xml)

---

## [1.0.0] - 2025-10-15

### ✨ Initial Release

#### Features
- ✅ Automatic detection of `.prisma` files in projects
- ✅ Graphical visualization of Prisma schema models
- ✅ Display of model relationships (One-to-One, One-to-Many, Many-to-Many)
- ✅ Live updates when schema files change
- ✅ Support for multiple schema files
- ✅ Interactive graph navigation:
  - Zoom with mouse wheel
  - Pan with right-click drag
  - Model selection by clicking
  - Model movement by dragging
- ✅ Dedicated tool window for visualization
- ✅ SynthWave '84 theme with neon aesthetics

#### Technical Details
- Language: Kotlin 2.0.21
- Target JVM: Java 17
- Build System: Gradle 8.5
- IDE Platform: IntelliJ IDEA 2025.1.3
- Build Range: 251-253.*
- Supported IDEs: 
  - IntelliJ IDEA
  - WebStorm
  - PHPStorm
  - PyCharm
  - GoLand
  - RubyMine
  - CLion
  - DataGrip
  - Rider
  - Android Studio

#### Core Components
- `PrismaSchemaParser.kt` - Schema parsing and validation
- `PrismaGraphPanel.kt` - Graph visualization and rendering
- `PrismaGraphToolWindowFactory.kt` - Tool window management
- `PrismaSchemaService.kt` - Project-level service
- `PrismaFileType.kt` - File type registration
- `PrismaIconProvider.kt` - Icon handling

#### Known Limitations (v1.0.0)
- ⚠️ Mac trackpad zoom behavior requires adjustment (FIXED in v1.0.1)
- ⚠️ Icon loading not fully implemented (FIXED in v1.0.1)
- ⚠️ Model positioning boundaries too restrictive (FIXED in v1.0.1)

---

## Upcoming

### Planned for Future Releases
- [ ] Bi-directional relationship visualization
- [ ] Model search and filtering
- [ ] Export graph as image (PNG, SVG)
- [ ] Custom color themes
- [ ] Advanced layout algorithms
- [ ] Model field type validation
- [ ] Integration with Prisma language server
- [ ] Performance optimizations for large schemas
- [ ] Mobile/touch optimization

### Community Feedback Welcome
We actively consider user feedback for future improvements. Please report issues or suggest features through GitHub Issues.

---

## Commit History

### v1.0.1 Release Commits
```
[Icon Fix] Create and integrate Prisma SVG icons
[Zoom Fix] Reduce scale factor and add event throttling for Mac support
[Dragging Fix] Remove position boundary constraints
[Version] Bump to 1.0.1
[Docs] Add comprehensive release notes and deployment guide
```

### v1.0.0 Release Commits
```
[Initial] First release of Prisma Graph Visualizer plugin
[UI] SynthWave theme implementation
[Parser] Prisma schema parsing engine
[Features] Graph visualization and interactions
```

---

## Migration Guide

### From v1.0.0 to v1.0.1

**For End Users:**
1. Uninstall v1.0.0: File → Settings → Plugins → Uninstall
2. Install v1.0.1: File → Settings → Plugins → Install Plugin from Disk
3. Restart IDE
4. All existing projects work without changes

**For Developers:**
1. Git pull latest changes
2. Run `./gradlew clean build`
3. All tests should pass
4. No code migration needed

---

## Support

- **Issues**: Report bugs on GitHub Issues
- **Discussions**: Ask questions on GitHub Discussions
- **Documentation**: See README.md and inline code comments
- **Contact**: alexander.koch@example.com

---

## License

MIT License - See LICENSE file for details

---

*Last Updated: 2025-11-01*