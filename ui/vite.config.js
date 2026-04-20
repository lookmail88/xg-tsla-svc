import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: './',
  server: {
    proxy: {
      '/xg-tsla-svc/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  }
})
