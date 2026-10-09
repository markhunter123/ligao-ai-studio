import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  build: {
    lib: {
      entry: resolve(__dirname, 'src/index.ts'),
      name: 'LigaoWebChat',
      formats: ['es', 'umd'],
      fileName: (format) => `ligao-ai-studio-webchat.${format}.js`,
    },
    rollupOptions: {
      output: {
        assetFileNames: 'ligao-ai-studio-webchat.[ext]',
      },
    },
    cssCodeSplit: false,
    minify: 'esbuild',
  },
})
