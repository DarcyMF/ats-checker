import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// The build goes straight into Spring Boot's static folder so the jar serves the page too.
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: "../src/main/resources/static",
    emptyOutDir: true,
  },
  server: {
    proxy: { "/api": "http://localhost:8080" },
  },
});
