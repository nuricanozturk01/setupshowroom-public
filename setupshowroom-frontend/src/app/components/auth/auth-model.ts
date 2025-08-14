export interface RegisterForm {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  refresh_token: string;
}

export interface LoginForm {
  username_or_email: string;
  password: string;
}

export interface CustomJwtPayload {
  iss?: string;
  sub?: string;
  aud?: string[] | string;
  exp?: number;
  nbf?: number;
  iat?: number;
  jti?: string;
  email?: string;
}
