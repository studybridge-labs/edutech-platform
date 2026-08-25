import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import {
    QueryClient,
    QueryClientProvider,
} from "@tanstack/react-query";

import AppRouter from "./app/AppRouter";
import "./index.css";

/**
 * 서버 상태 관리를 담당하는 TanStack Query Client입니다.
 *
 * 애플리케이션 전체에서 API 요청 결과를 캐싱하고
 * 요청 상태를 관리할 때 사용합니다.
 */
const queryClient = new QueryClient();

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <AppRouter />
            </BrowserRouter>
        </QueryClientProvider>
    </StrictMode>,
);