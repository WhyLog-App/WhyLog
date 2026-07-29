# WhyLog



[![React](https://img.shields.io/badge/React-19.2-61dafb?logo=react)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178c6?logo=typescript)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-7.3-646cff?logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind-4.2-38bdf8?logo=tailwindcss)](https://tailwindcss.com/)
[![Biome](https://img.shields.io/badge/Biome-2.4-60a5fa?logo=biome)](https://biomejs.dev/)

## ✨ Features

This repository is 🔋 battery packed with:

- ⚡️ **[Vite](https://vitejs.dev/)** - Next generation frontend tooling
- ⚛️ **[React 19](https://react.dev/)** - Latest React with modern features
- ✨ **[TypeScript](https://www.typescriptlang.org/)** - Type safety and better DX
- 💨 **[Tailwind CSS 4](https://tailwindcss.com/)** - Utility-first CSS framework
- 🦀 **[TanStack Query](https://tanstack.com/query)** - Powerful data synchronization
- 🌐 **[Axios](https://axios-http.com/)** - HTTP client with interceptors
- 🛣️ **[React Router](https://reactrouter.com/)** - Client-side routing
- 🔨 **[Biome](https://biomejs.dev/)** - Fast formatter and linter
- 📦 **[pnpm](https://pnpm.io/)** - Fast, disk space efficient package manager
- 📁 **Well-organized folder structure** - Scalable and maintainable architecture
- 🔐 **Authentication ready** - JWT token injection setup
- 🎯 **Path aliases** - Clean imports with `@/` prefix
- 🔧 **Strict TypeScript config** - `verbatimModuleSyntax` enabled

## 📁 Project Structure

```
src/
├── assets/          # Static assets (images, icons, etc.)
├── components/      # Reusable UI components
│   ├── header/
│   ├── footer/
│   └── hero/
├── constants/       # App constants and endpoints
├── hooks/           # Custom React hooks and Query context
├── layout/          # Layout components
├── pages/           # Page components
├── router/          # Route configuration
├── styles/          # Global styles and CSS
└── utils/           # Utility functions and HTTP client
```

## 🚀 Getting Started

### Prerequisites

- Node.js 18+ and pnpm

### Installation

1. Clone the repository:
```bash
git clone https://github.com/WhyLog-App/WhyLog-FE.git
```

2. Install dependencies:
```bash
pnpm install
```

3. Create a `.env` file in the root directory:
```env
VITE_API_BASE_URL=http://localhost/api
```

### Development

Start the development server:
```bash
pnpm start
```

The app will be available at `http://localhost:3000`

### Build

Build for production:
```bash
pnpm build
```

### Preview

Preview the production build:
```bash
pnpm preview
```

### Linting & Formatting

Run linter:
```bash
pnpm lint
```

Format code:
```bash
pnpm format
```

Check and fix both linting and formatting:
```bash
pnpm check
```

## 🛠️ Configuration

### Environment Variables

Create a `.env` file in the root directory with the following variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Base URL for API requests | `http://localhost/api` |

### Path Aliases

The project is configured with path aliases for cleaner imports:

```typescript
// Instead of
import { http } from '../../../utils/http';

// You can use
import { http } from '@/utils/http';
```

### HTTP Client

The project includes a pre-configured Axios instance with:
- JWT token injection via interceptors
- Global error handling
- TypeScript support
- Base URL configuration

Example usage:
```typescript
import { http } from '@/utils/http';

// GET request
const response = await http.get('/users');

// POST request
const response = await http.post('/users', { name: 'John' });
```

### Code Quality

The project uses **Biome** for fast and consistent code quality:

- **Formatter**: 2-space indentation, 80 character line width, double quotes
- **Linter**: Recommended rules enabled
- **Auto-organize imports**: Automatically sorts and removes unused imports
- **Git integration**: Respects `.gitignore` files

Configuration is in [biome.json](biome.json).

## 📦 Tech Stack Details

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 19.2.4 | UI library |
| TypeScript | 5.9.3 | Type safety |
| Vite | 7.3.1 | Build tool |
| Tailwind CSS | 4.2.1 | Styling |
| TanStack Query | 5.90.21 | Data fetching |
| Axios | 1.13.6 | HTTP client |
| React Router | 7.13.1 | Routing |
| Biome | 2.4.7 | Linter & Formatter |

