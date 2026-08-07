import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // Dynamically resolve base path: 
  // - On GitHub Actions (deploying to shuvm.me/cicd-cli/ subpath), use '/cicd-cli/'
  // - On Netlify, local dev, or other root-level deploys, use '/'
  base: process.env.GITHUB_ACTIONS ? '/cicd-cli/' : '/',
})
