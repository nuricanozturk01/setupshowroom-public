import { ProductGroup, Product } from './product.model';

export interface SystemSpecs {
  cpu: string;
  gpu: string;
  ram: string;
  storage: string;
  motherboard: string;
  psu: string;
  case: string;
  monitor: string;
  keyboard: string;
  mouse: string;
  headset: string;
  other: string;
  images?: string[];
  categories?: string[];
}

export interface FavoriteProductGroup {
  id: string;
  name: string;
  created_at: string;
  updated_at: string;
  products: FavoriteProduct[];
}

export interface FavoriteProduct {
  id: string;
  name: string;
  url: string;
  created_at: string;
  updated_at: string;
}

export interface UserProfileForm {
  username: string;
  full_name: string;
  email: string;
  profession?: string;
}

export interface UserInfo {
  id: string;
  full_name: string;
  username: string;
  email: string;
  profession?: string;
  enabled: boolean;
}

export interface ProfileInfo {
  id: string;
  username: string;
  email: string;
  full_name: string;
  profession?: string;
  profilePicture?: string;
  system_info?: SystemSpecs;
  product_groups?: ProductGroup[];
  products?: Product[];
}

export interface ApiResponse<T> {
  message: string;
  success: boolean;
  status: number;
  data: T;
}

