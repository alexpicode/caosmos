# Contributing to Caosmos 🪐

First off, thank you for considering contributing to **Caosmos**! It's people like you that make the simulation community such a great place.

We are building a high-performance simulation engine powered by AI, and we welcome explorers, architects, and developers who want to help us push the boundaries of emergent agent behavior.

## 🌈 How Can I Contribute?

### Reporting Bugs 🐛
- Search the [Issues](https://github.com/alexpicode/caosmos/issues) to see if the bug has already been reported.
- If you can't find an open issue, [open a new one](https://github.com/alexpicode/caosmos/issues/new).
- Include a clear title and description, as much relevant information as possible, and a code sample or an executable test case demonstrating the expected behavior that is not occurring.

### Suggesting Enhancements ✨
- Open an [issue](https://github.com/alexpicode/caosmos/issues/new) to discuss the enhancement.
- Describe the current behavior and what you would like to see instead.
- Explain why this enhancement would be useful to most users.

### Pull Requests 🚀
- **Branching**: Create a feature branch from `develop`.
- **Commits**: Follow [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) (e.g., `feat: add new action handler`, `fix: correct biology decay logic`).
- **Tests**: Ensure your changes don't break existing functionality. Run `./mvnw test` before submitting.
- **PR Target**: Always open your PR against the `develop` branch.

## 🛠️ Development Setup

1. **Clone the repo**:
   ```bash
   git clone https://github.com/alexpicode/caosmos.git
   cd caosmos
   ```
2. **Prerequisites**: 
   - **JDK 25** (Mandatory for Virtual Threads support).
   - **Docker** (Required for local LLM support via Ollama or database dependencies).
3. **Set up Environment**:
   Copy `.env.example` to `.env` and configure your `GOOGLE_AI_API_KEY` (if using Gemini).
4. **Build and Test**:
   ```bash
   ./mvnw clean install
   ```

## 🏗️ Technical Stack

- **Java 25**: Leveraging Virtual Threads (Project Loom) for high-concurrency simulation.
- **Spring Boot 4 / Modulith**: Structured as a Modular Monolith.
- **Spring AI**: Integration with Google GenAI and Ollama.
- **Project Lombok**: To reduce boilerplate.
- **Maven**: Dependency management and build lifecycle.

## 📏 Coding Standards

- Follow **Clean Architecture** principles.
- Use **Java Records** for DTOs and immutable state representation.
- Respect **Module Boundaries**: Do not introduce circular dependencies between Spring Modulith modules.
- **Strategy Pattern**: Implement `ActionHandler` for new world interactions.
- Document complex AI arbitration logic to ensure maintainability.

## 💬 Community

If you want to discuss architectural decisions, emergent behaviors, or just say hi, feel free to reach out via [email](mailto:alexpicode@proton.me) or open a discussion issue.

---
*Happy coding, Architect!*
