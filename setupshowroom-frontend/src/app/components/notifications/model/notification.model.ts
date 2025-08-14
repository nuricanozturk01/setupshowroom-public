import {UserInfo} from '../../profile/model/profile.model';
import { SetupInfo } from '../../setup/setup-model';

export type NotificationType = 'LIKE' | 'FAVORITE' | 'COMMENT' | 'FOLLOW' | 'MENTION' | 'REPLY' | 'REPOST' | 'TAG' | 'SYSTEM';

export interface Notification {
    id: string;
    title: string;
    description: string;
    type: NotificationType;
    action: string;
    user: UserInfo
    read: boolean;
    createdAt: Date;
    setup?: SetupInfo;
}
