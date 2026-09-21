import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8085', changeOrigin: true },
      '/ws': { target: 'http://localhost:8085', ws: true, changeOrigin: true }
    }
  }
})
