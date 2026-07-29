import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState } from "react";

// biome-ignore lint/suspicious/noExplicitAny: React children prop accepts various types
function QueryProvider({ children }: any) {
  const [client] = useState(
    new QueryClient({
      defaultOptions: {
        queries: {
          refetchOnWindowFocus: false,
        },
      },
    }),
  );

  return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
}

export { QueryProvider };
