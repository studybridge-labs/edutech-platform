/**
 * Access Token을 브라우저 Storage가 아닌
 * JavaScript 메모리에만 보관합니다.
 *
 * localStorage / sessionStorage에 저장하지 않아
 * Token의 장기 노출 위험을 줄입니다.
 */
let accessToken: string | null = null;

export function getAccessToken(): string | null {
    return accessToken;
}

export function setAccessToken(token: string): void {
    accessToken = token;
}

export function clearAccessToken(): void {
    accessToken = null;
}