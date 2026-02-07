Website: https://vaikrorag.net

# VaikroRag — Retrieval-Augmented Generation (RAG) demo

A compact end-to-end RAG example: upload PDFs, chunk & embed text, store vectors in Qdrant, and answer questions with an LLM using retrieved context and citations.

This repo is a developer-focused demo and playground (React frontend + Spring Boot backend + Qdrant + MySQL) intended for experimentation and learning.

---

## Highlights
- Ingest PDF documents, chunk them, compute embeddings, and upsert vectors to Qdrant.
- RAG endpoint that retrieves context, calls a chat model, and returns an answer with source snippets.
- Simple React + Vite frontend: chat UI, file upload, documents list.
- Dev-focused tooling: Docker Compose for full-stack local dev, Vite proxy for backend integration, and diagnostic logging for ingestion issues.

---

## Tech stack
- Backend: Spring Boot (Java), Maven
- Frontend: React + Vite + Tailwind
- Vector DB: Qdrant
- Database: MySQL
- Embeddings / LLM: configured to use OpenAI-compatible APIs
- Orchestration: Docker Compose

---

## What’s implemented
- Document ingestion pipeline:
  - PDF text extraction, chunking, batched embeddings, and upsert to Qdrant.
  - Document and chunk metadata persisted in MySQL.
- RAG query flow:
  - Search Qdrant for top-k chunks, build context, call chat model, return answer + sources.
- Frontend:
  - Chat UI with message list, controlled textarea (Enter to submit, Shift+Enter newline), file upload and documents list with remove action.
- Robustness & diagnostics:
  - Sanitization of extracted chunk text to remove control characters and escape backslashes before Qdrant upsert.
  - Explicit JSON serialization of Qdrant payloads and diagnostic previews to debug malformed inputs.

---

## Development notes
- `frontend/src/App.jsx` and `frontend/src/components/*` contain the UI flow (messages, uploads, documents).
- `backend/src/main/java/.../DocumentService.java`, `QdrantService.java`, and `RagService.java` implement ingestion, qdrant upsert/search, and RAG orchestration.

