import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss()
  ],
  server: {
    proxy: (() => {
      const resolved = 'http://localhost:8000';

      return {
          // proxy endpoints
          '/documents': resolved,
          '/raganswer': resolved,
          '/getMessages': resolved,
          '/addMessage': resolved
        };
    })()
  }
})
