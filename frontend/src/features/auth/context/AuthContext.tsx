import {
    createContext,
    type ReactNode,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";

import { logoutRequest } from "../api/logout";
import { refreshAccessToken } from "../api/refresh";
import {
    clearAccessToken,
    getAccessToken,
    setAccessToken,
} from "../token/accessTokenStore";

interface AuthContextValue {
    isAuthenticated: boolean;
    isInitialized: boolean;
    login: (accessToken: string) => void;
    logout: () => Promise<void>;
}

const AuthContext =
    createContext<AuthContextValue | null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

/**
 * 애플리케이션 전체의 인증 상태를 관리합니다.
 *
 * Access Token은 JavaScript 메모리에 보관하고,
 * Refresh Token은 HttpOnly Cookie로 관리합니다.
 *
 * 페이지를 새로고침하여 Access Token이 사라진 경우에는
 * Refresh Token을 이용하여 인증 상태를 자동으로 복구합니다.
 */
export function AuthProvider({
                                 children,
                             }: AuthProviderProps) {
    const [isAuthenticated, setIsAuthenticated] =
        useState(() => getAccessToken() !== null);

    const [isInitialized, setIsInitialized] =
        useState(false);

    /**
     * React StrictMode 개발 환경에서
     * 인증 복구 요청이 중복 실행되는 것을 방지합니다.
     *
     * Refresh Token Rotation을 사용하고 있기 때문에
     * /refresh가 불필요하게 연속 호출되지 않도록 합니다.
     */
    const hasRestoredAuthentication = useRef(false);

    useEffect(() => {
        if (hasRestoredAuthentication.current) {
            return;
        }

        hasRestoredAuthentication.current = true;

        const restoreAuthentication = async () => {
            /**
             * 이미 메모리에 Access Token이 존재한다면
             * Refresh 요청을 다시 수행하지 않습니다.
             */
            if (getAccessToken()) {
                setIsAuthenticated(true);
                setIsInitialized(true);
                return;
            }

            try {
                /**
                 * HttpOnly Cookie의 Refresh Token을 이용해
                 * 새로운 Access Token을 발급받습니다.
                 */
                const response =
                    await refreshAccessToken();

                setAccessToken(
                    response.accessToken,
                );

                setIsAuthenticated(true);
            } catch {
                /**
                 * Refresh Token이 없거나 만료/폐기된 경우
                 * 비로그인 상태로 처리합니다.
                 */
                clearAccessToken();
                setIsAuthenticated(false);
            } finally {
                /**
                 * Refresh 성공 여부와 관계없이
                 * 최초 인증 확인이 끝났음을 표시합니다.
                 */
                setIsInitialized(true);
            }
        };

        void restoreAuthentication();
    }, []);

    /**
     * 로그인 API 성공 후 호출합니다.
     *
     * Access Token을 메모리에 저장하고
     * React 인증 상태를 로그인 상태로 변경합니다.
     */
    const login = (accessToken: string) => {
        setAccessToken(accessToken);
        setIsAuthenticated(true);
    };

    /**
     * Backend에 로그아웃을 요청합니다.
     *
     * Backend에서는:
     * 1. AuthSession revoke
     * 2. Refresh Token Cookie 삭제
     *
     * 이후 Frontend의 Access Token과
     * 인증 상태도 제거합니다.
     */
    const logout = async () => {
        await logoutRequest();

        clearAccessToken();
        setIsAuthenticated(false);
    };

    return (
        <AuthContext.Provider
            value={{
                isAuthenticated,
                isInitialized,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

/**
 * AuthContext를 편리하게 사용하기 위한 Custom Hook입니다.
 */
export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error(
            "useAuth는 AuthProvider 내부에서 사용해야 합니다.",
        );
    }

    return context;
}