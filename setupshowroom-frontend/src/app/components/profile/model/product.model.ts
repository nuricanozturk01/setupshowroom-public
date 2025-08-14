export interface Product {
  id: string;
  name: string;
  url: string;
  created_at: string;
  updated_at: string;
}

export interface ProductGroup {
  group_id: string;
  id: string;
  name: string;
  products: Product[];
  created_at: string;
  updated_at: string;
}

export interface CreateProductGroupRequest {
  name: string;
}

export interface CreateProductRequest {
  name: string;
  url: string;
  group_id: string;
}

export interface UpdateProductGroupRequest {
  name: string;
}

export interface UpdateProductRequest {
  name: string;
  url: string;
}
