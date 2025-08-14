import {
  Component,
  Input,
  ChangeDetectionStrategy,
  ViewChild,
  ElementRef,
  OnDestroy,
  AfterViewInit,
  ChangeDetectorRef,
  OnInit,
  Renderer2
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import Plyr from 'plyr';
import {CommentInfo, SetupInfo} from '../setup-model';
import {SetupService} from '../service/setup.service';
import {formatDate, getUserId, gravatarUrl} from '../../../utils/util';
import {Router} from '@angular/router';
import {ToastService} from '../../shared/toast/toast.service';
import {ApiResponse} from '../../profile/model/profile.model';

export interface PlyrOptions {
  controls: Array<string>;
  sources?: Array<{ src: string; type?: string }>;
  clickToPlay?: boolean;
  disableContextMenu?: boolean;
  debug?: boolean;
  autoplay?: boolean;
  muted?: boolean;
  volume?: number;
  speed?: { selected: number; options: number[] };
  quality?: { default: number; options: number[] };
  poster?: string;
  ratio?: string;
  fullscreen?: { enabled: boolean; fallback: boolean; iosNative: boolean };
  keyboard?: { focused: boolean; global: boolean };
  tooltips?: { controls: boolean; seek: boolean };
  seekTime?: number;
  title?: string;
  playsinline?: boolean;
}

@Component({
  selector: 'app-setup-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './setup-post.component.html',
  styleUrls: ['./setup-post.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SetupPostComponent implements AfterViewInit, OnDestroy, OnInit {
  @Input() post!: SetupInfo;
  @Input() setupId?: string;
  @ViewChild('videoPlayer') videoPlayer!: ElementRef;
  @ViewChild('deleteModal') deleteModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentModal') commentModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentDeleteModal') commentDeleteModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('reportModal') reportModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentsContainer') commentsContainer!: ElementRef;
  @ViewChild('setupReportModal') setupReportModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentEditModal') commentEditModal!: ElementRef<HTMLDialogElement>;

  // Core state
  public isProcessingLike = false;
  public currentMediaIndex = 0;
  public mediaType: 'image' | 'video' = 'image';
  public isBookmarked = false;
  public isLiked = false;
  public showHeartAnimation = false;
  public setupReportReason = '';

  // Comments state
  public showComments = false;
  public newComment = '';
  public displayedComments: CommentInfo[] = [];
  public isLoading = false;
  public hasMoreComments = true;
  public reportReason = '';
  public selectedComment: CommentInfo | null = null;
  public editCommentText = '';

  // Private state
  private player: Plyr | null = null;
  private setupIdToDelete: string | null = null;
  private pageSize = 10;
  private currentPage = 0;
  private readonly SWIPE_THRESHOLD = 50;
  private touchStartX = 0;
  private touchEndX = 0;

  constructor(
    private cdr: ChangeDetectorRef,
    private setupService: SetupService,
    private router: Router,
    private toastService: ToastService,
    private renderer: Renderer2
  ) {
  }

  ngOnInit() {
    if (this.setupId) {
      this.setupService.getSetupById(this.setupId).subscribe({
        next: (response: ApiResponse<SetupInfo>) => {
          if (response.success && response.status === 200) {
            this.post = response.data;
            this.initializePostState();
          }
        },
        error: (error) => console.error('Error loading setup:', error)
      });
    } else {
      this.initializePostState();
    }
    this.setupTouchEvents();
  }

  ngAfterViewInit() {
    this.initializeMedia();
  }

  ngOnDestroy() {
    this.cleanupPlayer();
  }

  private initializePostState(): void {
    this.isLiked = this.post.is_liked;
    this.isBookmarked = this.post.is_favorite;
    this.mediaType = this.detectMediaType(this.currentMedia);
    this.cdr.detectChanges();
  }

  private setupTouchEvents(): void {
    if (typeof window !== 'undefined') {
      this.renderer.listen('window', 'touchstart', (event: TouchEvent) => {
        this.touchStartX = event.touches[0].clientX;
      });

      this.renderer.listen('window', 'touchend', (event: TouchEvent) => {
        this.touchEndX = event.changedTouches[0].clientX;
        this.handleSwipe();
      });
    }
  }

  private handleSwipe(): void {
    const swipeDistance = this.touchEndX - this.touchStartX;
    if (Math.abs(swipeDistance) > this.SWIPE_THRESHOLD) {
      if (swipeDistance > 0 && this.currentMediaIndex > 0) {
        this.previousMedia();
      } else if (swipeDistance < 0 && this.currentMediaIndex < this.totalMediaCount - 1) {
        this.nextMedia();
      }
    }
  }

  // Media Management
  get currentMedia(): string {
    const totalImages = this.post.images?.length || 0;
    return this.currentMediaIndex < totalImages
      ? this.post.images[this.currentMediaIndex]
      : this.post.videos[this.currentMediaIndex - totalImages];
  }

  get totalMediaCount(): number {
    return (this.post.images?.length || 0) + (this.post.videos?.length || 0);
  }

  private detectMediaType(url: string): 'image' | 'video' {
    const videoExtensions = ['mp4', 'webm', 'ogg', 'mov', 'm3u8'];
    const extension = url.split('.').pop()?.toLowerCase() || '';
    return videoExtensions.includes(extension) ? 'video' : 'image';
  }

  private cleanupPlayer(): void {
    if (this.player) {
      // Remove event listeners before destroying
      const videoElement = this.videoPlayer?.nativeElement;
      if (videoElement) {
        videoElement.removeEventListener('error', () => {});
        videoElement.pause();
        videoElement.src = '';
      }
      this.player.destroy();
      this.player = null;
    }
  }

  private initializeMedia(): void {
    this.cleanupPlayer();

    if (this.mediaType === 'video' && this.videoPlayer?.nativeElement) {
      const videoElement: HTMLVideoElement = this.videoPlayer.nativeElement;

      // Preload metadata for better performance
      videoElement.preload = 'metadata';

      // Optimize video loading
      videoElement.load();

      // Set video attributes for better performance
      videoElement.setAttribute('playsinline', '');
      videoElement.setAttribute('webkit-playsinline', '');
      videoElement.setAttribute('muted', '');

      const plyrOptions: PlyrOptions = {
        controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'],
        clickToPlay: true,
        disableContextMenu: false,
        debug: false,
        autoplay: false,
        muted: true,
        playsinline: true,
        fullscreen: { enabled: true, fallback: true, iosNative: true },
        keyboard: { focused: true, global: true },
        tooltips: { controls: true, seek: true },
        seekTime: 10
      };

      // Initialize Plyr with optimized options
      this.player = new Plyr(videoElement, plyrOptions);

      // Add error handling for video loading
      videoElement.addEventListener('error', (e) => {
        console.error('Video loading error:', e);
        this.toastService.error('Failed to load video');
      });
    }
  }

  nextMedia(): void {
    if (this.currentMediaIndex < this.totalMediaCount - 1) {
      this.currentMediaIndex++;
      this.mediaType = this.detectMediaType(this.currentMedia);
      this.initializeMedia();
      this.cdr.detectChanges();
    }
  }

  previousMedia(): void {
    if (this.currentMediaIndex > 0) {
      this.currentMediaIndex--;
      this.mediaType = this.detectMediaType(this.currentMedia);
      this.initializeMedia();
      this.cdr.detectChanges();
    }
  }

  getVideoMimeType(url: string): string {
    const extension = url.split('.').pop()?.toLowerCase();
    const mimeTypes: { [key: string]: string } = {
      mp4: 'video/mp4',
      webm: 'video/webm',
      ogg: 'video/ogg',
      mov: 'video/quicktime'
    };
    return mimeTypes[extension || ''] || 'video/mp4';
  }

  // Interaction Handlers
  toggleBookmark(): void {
    this.isBookmarked ? this.removeFavorite() : this.bookmarkSetup();
  }

  toggleLike(): void {
    if (this.isProcessingLike) return;

    this.isProcessingLike = true;
    const originalState = this.isLiked;
    const originalLikes = this.post.likes;

    // Optimistic update
    this.isLiked = !this.isLiked;
    this.post.likes = this.isLiked ? this.post.likes + 1 : Math.max(0, this.post.likes - 1);
    this.cdr.detectChanges();

    const apiCall = this.isLiked
      ? this.setupService.likeSetup(this.post.id)
      : this.setupService.unlikeSetup(this.post.id);

    apiCall.subscribe({
      next: (response) => {
        if (!response.success) {
          this.isLiked = originalState;
          this.post.likes = originalLikes;
        }
        this.isProcessingLike = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLiked = originalState;
        this.post.likes = originalLikes;
        this.isProcessingLike = false;
        this.cdr.detectChanges();
      }
    });
  }

  onImageDoubleClick(): void {
    this.showHeartAnimation = true;
    setTimeout(() => {
      this.showHeartAnimation = false;
      this.cdr.detectChanges();
    }, 1500);

    if (!this.isLiked) {
      this.toggleLike();
    }
  }

  // Comments Management
  toggleComments(): void {
    this.openCommentModal();
    this.showComments = !this.showComments;
    if (this.showComments && this.displayedComments.length === 0) {
      this.loadMoreComments();
    }
  }

  loadMoreComments(): void {
    if (this.isLoading || !this.hasMoreComments) return;

    this.isLoading = true;
    this.setupService.findCommentsBySetupId(this.post.id, this.currentPage, this.pageSize)
      .subscribe({
        next: response => {
          if (response.success && response.status === 200) {
            const newComments = response.data;
            if (newComments.length > 0) {
              this.displayedComments = [...this.displayedComments, ...newComments];
              this.currentPage++;
              this.hasMoreComments = newComments.length === this.pageSize;
            } else {
              this.hasMoreComments = false;
            }
          } else {
            this.hasMoreComments = false;
          }
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.isLoading = false;
          this.toastService.error('Failed to load comments');
        }
      });
  }

  addComment(): void {
    if (!this.newComment.trim()) return;

    if (this.newComment.length < 5 || this.newComment.length > 300) {
      this.toastService.warning('Comment must be between 5 and 300 characters');
      return;
    }

    const tempComment: CommentInfo = {
      id: 'temp-' + Date.now(),
      content: this.newComment.trim(),
      created_at: new Date().toISOString(),
      author: this.post.user_info,
      like_count: 0,
      is_liked: false,
      isProcessingLike: false
    };

    this.displayedComments = [tempComment, ...this.displayedComments];
    this.newComment = '';
    this.post.comment_size++;
    this.cdr.detectChanges();

    this.setupService.postComment(this.post.id, {content: tempComment.content})
      .subscribe({
        next: response => {
          if (response.success) {
            const index = this.displayedComments.findIndex(c => c.id === tempComment.id);
            if (index !== -1) {
              this.displayedComments[index] = {
                ...response.data,
                created_at: new Date().toISOString()
              };
              this.cdr.detectChanges();
            }
          }
        },
        error: () => {
          this.displayedComments = this.displayedComments.filter(c => c.id !== tempComment.id);
          this.post.comment_size--;
          this.toastService.error('Failed to post comment');
          this.cdr.detectChanges();
        }
      });
  }

  toggleCommentLike(comment: CommentInfo): void {
    if (comment.isProcessingLike) return;

    comment.isProcessingLike = true;
    const originalState = comment.is_liked;
    const originalLikes = comment.like_count;

    comment.is_liked = !comment.is_liked;
    comment.like_count = comment.is_liked ? comment.like_count + 1 : Math.max(0, comment.like_count - 1);
    this.cdr.detectChanges();

    const apiCall = comment.is_liked
      ? this.setupService.likeComment(this.post.id, comment.id)
      : this.setupService.unlikeComment(this.post.id, comment.id);

    apiCall.subscribe({
      next: (response) => {
        if (!response.success) {
          comment.is_liked = originalState;
          comment.like_count = originalLikes;
        }
        comment.isProcessingLike = false;
        this.cdr.detectChanges();
      },
      error: () => {
        comment.is_liked = originalState;
        comment.like_count = originalLikes;
        comment.isProcessingLike = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Modal Management
  openDeleteModal(setupId: string): void {
    this.setupIdToDelete = setupId;
    this.deleteModal?.nativeElement.showModal();
  }

  closeDeleteModal(): void {
    this.setupIdToDelete = null;
    this.deleteModal?.nativeElement.close();
  }

  confirmDelete(): void {
    if (!this.setupIdToDelete) return;

    this.setupService.deleteSetup(this.setupIdToDelete).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/my-setups']).then(() => {
            this.toastService.success('Setup deleted successfully');
            setTimeout(() => window.location.reload(), 400);
          });
        } else {
          this.toastService.error('Failed to delete the setup');
        }
        this.closeDeleteModal();
      },
      error: () => {
        this.toastService.error('An error occurred while deleting');
        this.closeDeleteModal();
      }
    });
  }

  openCommentModal(): void {
    this.commentModal?.nativeElement.showModal();
    if (this.displayedComments.length === 0) {
      this.loadMoreComments();
    }
  }

  openCommentDeleteModal(comment: CommentInfo): void {
    this.selectedComment = comment;
    this.commentDeleteModal?.nativeElement.showModal();
  }

  closeCommentDeleteModal(): void {
    this.selectedComment = null;
    this.commentDeleteModal?.nativeElement.close();
  }

  confirmCommentDelete(): void {
    if (!this.selectedComment) return;

    const isCommentOwner = this.selectedComment.author.id === getUserId();

    if (!isCommentOwner) {
      this.toastService.error('You can only delete your own comments');
      return;
    }

    this.setupService.deleteComment(this.post.id, this.selectedComment.id).subscribe({
      next: (response) => {
        if (response.success) {
          this.displayedComments = this.displayedComments.filter(c => c.id !== this.selectedComment?.id);
          this.post.comment_size--;
          this.toastService.success('Comment deleted successfully');
        } else {
          this.toastService.error('Failed to delete the comment');
        }
        this.closeCommentDeleteModal();
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('An error occurred while deleting the comment');
        this.closeCommentDeleteModal();
      }
    });
  }

  openReportModal(comment: CommentInfo): void {
    this.selectedComment = comment;
    this.reportModal?.nativeElement.showModal();
  }

  closeReportModal(): void {
    this.selectedComment = null;
    this.reportReason = '';
    this.reportModal?.nativeElement.close();
  }

  submitReport(): void {
    if (!this.selectedComment || !this.reportReason.trim()) return;

    if (this.reportReason.length < 10 || this.reportReason.length > 500) {
      this.toastService.warning('Report description must be between 10 and 500 characters');
      return;
    }

    const reportForm = {
      type: "COMMENT",
      description: this.reportReason.trim(),
      reported_item_id: this.selectedComment.id
    };

    this.setupService.reportComment(reportForm).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastService.success('Comment reported successfully');
        } else {
          this.toastService.error('Failed to report the comment');
        }
        this.closeReportModal();
      },
      error: () => {
        this.toastService.error('An error occurred while reporting the comment');
        this.closeReportModal();
      }
    });
  }

  // Setup Report Management
  openSetupReportModal(): void {
    if (this.post.user_info.id === getUserId()) {
      this.toastService.error('You cannot report your own setup');
      return;
    }
    this.setupReportModal?.nativeElement.showModal();
  }

  closeSetupReportModal(): void {
    this.setupReportReason = '';
    this.setupReportModal?.nativeElement.close();
  }

  submitSetupReport(): void {
    if (!this.setupReportReason.trim()) return;

    if (this.setupReportReason.length < 10 || this.setupReportReason.length > 500) {
      this.toastService.warning('Report description must be between 10 and 500 characters');
      return;
    }

    const reportForm = {
      type: "POST",
      description: this.setupReportReason.trim(),
      reported_item_id: this.post.id
    };

    this.setupService.reportSetup(reportForm).subscribe({
      next: (response) => {
        if (response.success) {
          this.toastService.success('Setup reported successfully');
        } else {
          this.toastService.error('Failed to report the setup');
        }
        this.closeSetupReportModal();
      },
      error: () => {
        this.toastService.error('An error occurred while reporting the setup');
        this.closeSetupReportModal();
      }
    });
  }

  // Navigation
  onEditPost(setupId: string): void {
    this.router.navigate(['/setup', setupId]);
  }

  navigateUser(): void {
    window.location.href = `/profile/${this.post.user_info.username}`;
  }

  navigateUserProfileUser(username: string): void {
    window.location.href = `/profile/${username}`;
  }

  // Utility functions
  onScroll(event: Event): void {
    const element = event.target as HTMLElement;
    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 100;

    if (atBottom && !this.isLoading && this.hasMoreComments) {
      this.loadMoreComments();
    }
  }

  private bookmarkSetup(): void {
    this.setupService.addFavorite(this.post.id).subscribe({
      next: response => {
        if (response.success && response.status === 200) {
          this.post.is_favorite = true;
          this.isBookmarked = true;
          this.toastService.success('Setup added to bookmarks');
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.isBookmarked = false;
        this.toastService.error('Failed to bookmark setup');
        this.cdr.detectChanges();
      }
    });
  }

  private removeFavorite(): void {
    this.setupService.removeFavorite(this.post.id).subscribe({
      next: response => {
        if (response.success && response.status === 200) {
          this.post.is_favorite = false;
          this.isBookmarked = false;
          this.toastService.success('Setup removed from bookmarks');
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.isBookmarked = true;
        this.toastService.error('Failed to remove bookmark');
        this.cdr.detectChanges();
      }
    });
  }

  // Style helpers
  getCategoryClass(category: string): string {
    const baseClasses = 'badge badge-md font-medium transition-all duration-300 hover:scale-105';
    const categoryClasses: { [key: string]: string } = {
      workspace: `${baseClasses} badge-primary text-primary-content`,
      gaming: `${baseClasses} badge-secondary text-secondary-content`,
      streaming: `${baseClasses} badge-accent text-accent-content`,
      development: `${baseClasses} badge-info text-info-content`
    };
    return categoryClasses[category.toLowerCase()] || `${baseClasses} badge-neutral text-neutral-content`;
  }

  getTagClass(type: string): string {
    const baseClasses = 'badge badge-md font-medium transition-all duration-300 hover:scale-105';
    const normalizedType = type.toLowerCase();

    if (normalizedType.includes('monitor') || normalizedType.includes('display')) {
      return `${baseClasses} badge-primary text-primary-content`;
    }
    if (normalizedType.includes('keyboard') || normalizedType.includes('mouse')) {
      return `${baseClasses} badge-secondary text-secondary-content`;
    }
    if (normalizedType.includes('desk') || normalizedType.includes('chair')) {
      return `${baseClasses} badge-accent text-accent-content`;
    }
    if (normalizedType.includes('audio') || normalizedType.includes('speaker') || normalizedType.includes('headphone')) {
      return `${baseClasses} badge-info text-info-content`;
    }
    if (normalizedType.includes('light') || normalizedType.includes('led')) {
      return `${baseClasses} badge-warning text-warning-content`;
    }
    if (normalizedType.includes('software') || normalizedType.includes('app')) {
      return `${baseClasses} badge-success text-success-content`;
    }
    return `${baseClasses} badge-neutral text-neutral-content`;
  }

  // Exposed utility functions
  protected readonly gravatarUrl = gravatarUrl;
  protected readonly formatDate = formatDate;
  protected readonly isOwner = () => this.post.user_info.id === getUserId();
  protected readonly getUserId = getUserId;

  // Comment Edit Management
  openCommentEditModal(comment: CommentInfo): void {
    if (comment.author.id !== getUserId()) {
      this.toastService.error('You can only edit your own comments');
      return;
    }
    this.selectedComment = comment;
    this.editCommentText = comment.content;
    this.commentEditModal?.nativeElement.showModal();
  }

  closeCommentEditModal(): void {
    this.selectedComment = null;
    this.editCommentText = '';
    this.commentEditModal?.nativeElement.close();
  }

  saveCommentEdit(): void {
    if (!this.selectedComment || !this.editCommentText.trim()) return;

    if (this.editCommentText.length < 5 || this.editCommentText.length > 300) {
      this.toastService.warning('Comment must be between 5 and 300 characters');
      return;
    }

    const commentForm = {
      content: this.editCommentText.trim()
    };

    this.setupService.updateComment(this.post.id, this.selectedComment.id, commentForm).subscribe({
      next: (response) => {
        if (response.success) {
          //set updated comment
          const index = this.displayedComments.findIndex(c => c.id === this.selectedComment?.id);
          if (index !== -1) {
            this.displayedComments[index] = {
              ...response.data,
              created_at: new Date().toISOString()
            };
            this.cdr.detectChanges();
          }
          this.toastService.success('Comment updated successfully');
        } else {
          this.toastService.error('Failed to update the comment');
        }
        this.closeCommentEditModal();
        this.cdr.detectChanges();
      },
      error: () => {
        this.toastService.error('An error occurred while updating the comment');
        this.closeCommentEditModal();
      }
    });
  }

  protected readonly localStorage = localStorage;
}
