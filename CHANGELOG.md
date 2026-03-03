# Changelog

## v0.1.0 - 2026-03-03

### Added
- Initial reusable project structure for LexCupStudio (multi-module Maven).
- `core` module with frontend pipeline contracts:
  - `LexerAdapter`
  - `ParserAdapter`
  - `LanguageFrontend`
  - `FrontendResult`
  - `FrontendMessage`
- `starter` module with base templates and adapter guide.
- `example-language` module with end-to-end JFlex + CUP integration:
  - lexer and parser generation in Maven build
  - adapters wired to `core`
  - integration tests
- `example-language-archetype` module to generate new CUP/Flex starter projects using Maven Archetype.

### Notes
- This is the first stable release tag for bootstrapping new language projects.
