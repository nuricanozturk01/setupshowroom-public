import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  Renderer2
} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgForOf, NgIf, NgOptimizedImage} from '@angular/common';
import {gravatarUrl, getUserId} from '../../../utils/util';
import {CommentForm, CommentInfo, SetupInfo} from '../setup-model';
import Plyr from 'plyr';
import {SetupService} from '../service/setup.service';
import {ActivatedRoute, Router} from '@angular/router';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import { ToastService } from '../../shared/toast/toast.service';

export interface PlyrSource {
  src: string;
  type?: string;
  provider?: string;
}

export interface PlyrOptions {
  controls: Array<string>;
  sources?: PlyrSource[];
  clickToPlay?: boolean;
  disableContextMenu?: boolean;
  debug?: boolean;
}

@Component({
  selector: 'app-setup-info',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    NgOptimizedImage
  ],
  templateUrl: './setup-info.component.html',
  styleUrl: './setup-info.component.scss'
})
export class SetupInfoComponent implements AfterViewInit, OnDestroy, OnInit {
  @ViewChild('commentsContainer') commentsContainer!: ElementRef;
  @ViewChild('videoPlayer') videoPlayer!: ElementRef;
  @ViewChild('deleteModal') deleteModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentModal') commentModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentDeleteModal') commentDeleteModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('reportModal') reportModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('setupReportModal') setupReportModal!: ElementRef<HTMLDialogElement>;
  @ViewChild('commentEditModal') commentEditModal!: ElementRef<HTMLDialogElement>;

  // Media Control
  public currentMediaIndex = 0;
  public mediaType: 'image' | 'video' = 'image';
  private player: Plyr | null = null;
  private touchStartX: number = 0;
  private touchEndX: number = 0;
  private readonly SWIPE_THRESHOLD = 50;

  // Loading State
  public isLoadingSetup = true;

  // Comments
  public newComment = '';
  private pageSize = 10;
  private currentPage = 0;
  public displayedComments: CommentInfo[] = [];
  public hasMoreComments = true;

  // Setup Data
  public post!: SetupInfo;
  public isBookmarked = false;
  public isLiked = false;

  // Setup Management
  public setupReportReason = '';
  public reportReason = '';
  public selectedComment: CommentInfo | null = null;
  public editCommentText = '';
  private setupIdToDelete: string | null = null;

  // Expose getUserId for template
  protected readonly getUserId = getUserId;

  constructor(
    private cdr: ChangeDetectorRef,
    private setupService: SetupService,
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer,
    private renderer: Renderer2,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadSetupData(id);
    }
    this.setupTouchEvents();
  }

  // Touch events for swipe navigation
  private setupTouchEvents() {
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

  private handleSwipe() {
    const swipeDistance = this.touchEndX - this.touchStartX;
    if (Math.abs(swipeDistance) > this.SWIPE_THRESHOLD) {
      if (swipeDistance > 0 && this.currentMediaIndex > 0) {
        this.previousMedia();
      } else if (swipeDistance < 0 && this.currentMediaIndex < this.totalMediaCount - 1) {
        this.nextMedia();
      }
    }
  }

  private loadSetupData(id: string): void {
    this.isLoadingSetup = true;
    this.setupService.getSetupById(id).subscribe(response => {
      if (response.success && response.status === 200) {
        this.post = response.data;
        this.isLiked = this.post.is_liked;
        this.isBookmarked = this.post.is_favorite;
        this.loadInitialComments(id);
      } else {
        console.error('Error loading setup:', response);
      }
      this.isLoadingSetup = false;
      this.cdr.detectChanges();
    });
  }

  private loadInitialComments(setupId: string): void {
    this.setupService.findCommentsBySetupId(setupId, this.currentPage, this.pageSize)
      .subscribe(comments => {
        if (comments.success && comments.status === 200) {
          this.displayedComments = comments.data;
          this.hasMoreComments = comments.data.length === this.pageSize;
        } else {
          this.hasMoreComments = false;
        }
      });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initializeMedia();
      this.cdr.detectChanges();
    }, 0);
  }

  ngOnDestroy() {
    if (this.player) {
      this.player.destroy();
    }
  }

  private initializeMedia(): void {
    if (this.mediaType === 'video' && this.videoPlayer?.nativeElement) {
      if (this.player) {
        this.player.destroy();
        this.player = null;
      }

      const videoElement: HTMLVideoElement = this.videoPlayer.nativeElement;
      const plyrOptions: PlyrOptions = {
        controls: ['play-large', 'play', 'progress', 'current-time', 'mute', 'volume', 'fullscreen'],
        clickToPlay: true,
        disableContextMenu: false,
        debug: false,
        sources: [{
          src: this.currentMedia,
          type: this.getVideoMimeType(this.currentMedia)
        }]
      };

      this.player = new Plyr(videoElement, plyrOptions);
    }
  }

  // Media Navigation
  get currentMedia(): string {
    if (this.currentMediaIndex < this.post.images.length) {
      this.mediaType = 'image';
      return this.post.images[this.currentMediaIndex];
    } else {
      this.mediaType = 'video';
      const videoIndex = this.currentMediaIndex - this.post.images.length;
      setTimeout(() => {
        this.initializeMedia();
        this.cdr.detectChanges();
      }, 100);
      return this.post.videos[videoIndex];
    }
  }

  get totalMediaCount(): number {
    return this.post.images.length + this.post.videos.length;
  }

  nextMedia(): void {
    if (this.currentMediaIndex < this.totalMediaCount - 1) {
      this.currentMediaIndex++;
      this.cdr.detectChanges();
    }
  }

  previousMedia(): void {
    if (this.currentMediaIndex > 0) {
      this.currentMediaIndex--;
      this.cdr.detectChanges();
    }
  }

  // Video helpers
  isExternalVideo(url: string): boolean {
    return url.includes('youtube.com') ||
      url.includes('youtu.be') ||
      url.includes('vimeo.com');
  }

  getVideoMimeType(url: string): string {
    const extension = url.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'mp4':
        return 'video/mp4';
      case 'webm':
        return 'video/webm';
      case 'ogg':
        return 'video/ogg';
      case 'mov':
        return 'video/quicktime';
      default:
        return 'video/mp4';
    }
  }

  getSafeVideoUrl(url: string): SafeResourceUrl {
    if (url.includes('youtube.com') || url.includes('youtu.be')) {
      const videoId = this.getYouTubeVideoId(url);
      url = `https://www.youtube.com/embed/${videoId}`;
    } else if (url.includes('vimeo.com')) {
      const videoId = this.getVimeoVideoId(url);
      url = `https://player.vimeo.com/video/${videoId}`;
    }
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  private getYouTubeVideoId(url: string): string {
    let videoId = '';
    if (url.includes('youtube.com')) {
      videoId = url.split('v=')[1];
      const ampersandPosition = videoId.indexOf('&');
      if (ampersandPosition !== -1) {
        videoId = videoId.substring(0, ampersandPosition);
      }
    } else if (url.includes('youtu.be')) {
      videoId = url.split('youtu.be/')[1];
    }
    return videoId;
  }

  private getVimeoVideoId(url: string): string {
    let videoId = '';
    if (url.includes('vimeo.com')) {
      videoId = url.split('vimeo.com/')[1];
      const questionMarkPosition = videoId.indexOf('?');
      if (questionMarkPosition !== -1) {
        videoId = videoId.substring(0, questionMarkPosition);
      }
    }
    return videoId;
  }

  // Comments Management
  loadMoreComments(): void {
    if (this.isLoadingSetup || !this.hasMoreComments) return;

    this.isLoadingSetup = true;
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

          this.isLoadingSetup = false;
          this.cdr.markForCheck();
        },
        error: error => {
          console.error('Error loading comments:', error);
          this.isLoadingSetup = false;
        }
      });
  }

  onScroll(event: Event): void {
    const element = event.target as HTMLElement;
    const atBottom = element.scrollHeight - element.scrollTop <= element.clientHeight + 100;

    if (atBottom && !this.isLoadingSetup && this.hasMoreComments) {
      this.loadMoreComments();
    }
  }

  addComment(): void {
    if (!this.newComment.trim()) return;

    const commentForm: CommentForm = {
      content: this.newComment
    };

    this.setupService.postComment(this.post.id, commentForm)
      .subscribe({
        next: response => {
          if (response.success) {
            const newComment = response.data;
            this.displayedComments = [newComment, ...this.displayedComments];
            this.newComment = '';
            this.post.comment_size++;
            this.cdr.markForCheck();
          }
        },
        error: error => {
          console.error('Error posting comment:', error);
        }
      });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    // If the date is invalid (like Jan 1, 1970), return "just now"
    if (isNaN(date.getTime()) || date.getTime() === 0) {
      return 'just now';
    }

    if (diffInSeconds < 60) {
      return 'just now';
    }

    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
      return `${diffInMinutes}m ago`;
    }

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
      return `${diffInHours}h ago`;
    }

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) {
      return `${diffInDays}d ago`;
    }

    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  protected readonly gravatarUrl = gravatarUrl;

  toggleCommentLike(comment: CommentInfo) {
    if (comment.isProcessingLike) return;

    comment.isProcessingLike = true;
    const originalState = comment.is_liked;
    const originalLikes = comment.like_count;

    // Optimistic update
    comment.is_liked = !comment.is_liked;
    comment.like_count = comment.is_liked ? comment.like_count + 1 : Math.max(0, comment.like_count - 1);
    this.cdr.detectChanges();

    const apiCall = comment.is_liked
      ? this.setupService.likeComment(this.post.id, comment.id)
      : this.setupService.unlikeComment(this.post.id, comment.id);

    apiCall.subscribe({
      next: (response) => {
        if (!(response.success)) {
          comment.is_liked = originalState;
          comment.like_count = originalLikes;
          this.cdr.detectChanges();
        }
        comment.isProcessingLike = false;
      },
      error: (error) => {
        console.error('Comment like API error:', error);
        // Revert changes on error
        comment.is_liked = originalState;
        comment.like_count = originalLikes;
        comment.isProcessingLike = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Navigation
  navigateUserProfileUser(username: string): void {
    window.location.href = `/profile/${username}`;
  }

  // Setup Management
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

  onEditPost(setupId: string): void {
    this.router.navigate(['/setup', setupId]);
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

  // Comment Management
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

  // Add isOwner method
  isOwner(): boolean {
    return this.post?.user_info?.id === getUserId();
  }
}
