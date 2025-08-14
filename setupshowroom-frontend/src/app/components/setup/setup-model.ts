import {UserInfo} from '../profile/model/profile.model';

export interface SetupFormDTO {
  title: string;
  description: string;
  categories: string[];
  imageUrls: string[];
  videoUrls: string[];
  tags: string[];
}

export interface SetupInfo {
  id: string;
  title: string;
  description?: string;
  images: string[];
  videos: string[];
  categories: string[];
  tags: string[];
  likes: number;
  comment_size: number;
  created_at: string;
  updated_at: string;
  is_liked: boolean;
  is_favorite: boolean;
  user_info: UserInfo;
}

export interface CommentInfo {
  id: string;
  content: string;
  created_at: string;
  author: UserInfo;
  like_count: number;
  is_liked: boolean;
  isProcessingLike?: boolean;
}

export interface CommentForm {
  content: string;
}

export interface SetupUpdateForm {
  title: string;
  description: string;
  categories: string[];
  tags: string[];
  existing_images: string[];
  existing_videos: string[];
}
