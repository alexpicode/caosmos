# Contributing to Caosmos 🪐

Thank you for your interest in contributing to Caosmos! We are building a high-performance simulation engine powered by AI, and we welcome all kinds of contributions.

## 🛠️ Development Setup

1. **Prerequisites**: 
   - **JDK 25** (Mandatory for Virtual Threads).
   - **Docker** for running dependencies.
   - A **Google AI API Key** for Gemini.
2. **Clone the repo**: `git clone https://github.com/alexpicode/caosmos.git`
3. **Environment**: Copy `.env.example` to `.env` and set your `GOOGLE_AI_API_KEY`.
4. **Build**: Run `./mvnw clean install` to ensure everything is correct.

## 🏗️ Architecture & Style

We follow a **Modular Monolith** approach with **Clean Architecture**:
- **Modules**: Keep logic inside the appropriate module (`citizens`, `world`, `actions`, `directors`, `common`).
- **Strategy Pattern**: New world interactions should implement the `ActionHandler` strategy.
- **Spring Modulith**: Respect module boundaries to ensure the project remains maintainable.
- **Immutability**: Prefer using Java `record` types for data transfer and state.

## 🧪 Testing

- Before submitting a PR, ensure all tests pass: `./mvnw test`
- If you add a new feature, please include corresponding unit or integration tests.

## 📨 How to Submit a Contribution

1. **Fork** the repository.
2. Create a **Feature Branch** (`git checkout -b feature/amazing-feature`).
3. **Commit** your changes following [Conventional Commits](https://www.conventionalcommits.org/).
4. **Push** to the branch (`git push origin feature/amazing-feature`).
5. Open a **Pull Request** against the `develop` branch.

## 💬 Community & Communication

If you have questions, feel free to reach out via [alexpicode@proton.me](mailto:alexpicode@proton.me) or open a GitHub Issue for discussion before starting major changes.
