import * as https from "node:https";
import * as path from "node:path";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";
import svgr from "vite-plugin-svgr";

const API_TARGET = "api.whylog.site";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    {
      name: "api-proxy",
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          if (!req.url?.startsWith("/api")) return next();
          const {
            connection,
            "keep-alive": _ka,
            "transfer-encoding": _te,
            origin: _origin,
            referer: _referer,
            ...safeHeaders
          } = req.headers;
          const options: https.RequestOptions = {
            hostname: API_TARGET,
            port: 443,
            path: req.url,
            method: req.method,
            headers: { ...safeHeaders, host: API_TARGET },
          };
          const proxyReq = https.request(options, (proxyRes) => {
            const { "transfer-encoding": _tte, ...safeResHeaders } =
              proxyRes.headers;
            res.writeHead(proxyRes.statusCode ?? 500, safeResHeaders);
            proxyRes.pipe(res, { end: true });
          });
          proxyReq.on("error", (err) => {
            console.error("[api-proxy] upstream error:", err.message);
            next();
          });
          req.pipe(proxyReq, { end: true });
        });
      },
    },
    svgr(),
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: [{ find: "@", replacement: path.resolve(__dirname, "src") }],
  },
});
