# Contributing to OmniTab

Thank you for your interest in contributing to OmniTab! This project follows a modular NMS abstraction pattern.

## Guidelines
1. **Multi-Version Integrity**: Ensure your changes don't break compatibility with older versions (1.8.8) or modern versions (1.21.x).
2. **Code Style**: Follow standard Java naming conventions and use `@NotNull` annotations where appropriate.
3. **Pull Requests**:
   - Create a feature branch.
   - Describe your changes clearly.
   - Run `mvn clean package` to ensure the build succeeds across all modules.

## Module Structure
- `omnitab-api`: Define new interfaces here.
- `omnitab-common`: Logic shared across all versions.
- `omnitab-adapter-vX`: Version-specific packet handling.
- `omnitab-core`: Main plugin entry and shading.

## Questions?
Open an issue on the GitHub repository.
