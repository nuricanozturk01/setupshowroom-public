import {Component, OnInit, OnDestroy, HostListener} from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {SetupPostComponent} from "../setup-post/setup-post.component";
import {ActivatedRoute} from '@angular/router';
import {SetupService} from '../service/setup.service';
import {Subject, takeUntil} from 'rxjs';
import {SetupInfo} from '../setup-model';

@Component({
  selector: 'app-my-setup',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    SetupPostComponent
  ],
  templateUrl: './my-setup.component.html',
  styleUrl: './my-setup.component.scss'
})
export class MySetupComponent implements OnInit, OnDestroy {
  public posts: SetupInfo[] = [];
  public loading = false;
  public error: string | null = null;
  public currentPage = 0;
  public isLastPage = false;
  private destroy$ = new Subject<void>();
  private loadingMore = false;
  private readonly pageSize = 10;

  constructor(private route: ActivatedRoute,
              private setupService: SetupService) {
  }

  ngOnInit(): void {
    this.loadPosts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:scroll', [])
  onScroll(): void {
    if (this.loadingMore || this.isLastPage) {
      return;
    }

    const scrollPosition = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
    const windowHeight = window.innerHeight;
    const documentHeight = Math.max(
      document.body.scrollHeight,
      document.body.offsetHeight,
      document.documentElement.clientHeight,
      document.documentElement.scrollHeight,
      document.documentElement.offsetHeight
    );

    // Load more when user scrolls to bottom (with 100px threshold)
    if (documentHeight - (scrollPosition + windowHeight) < 100) {
      this.loadMore();
    }
  }

  private loadPosts(): void {
    if (this.loading) return;

    this.loading = true;
    this.error = null;

    this.setupService.findAllByUserIdPageable(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.status === 200) {
            this.posts = response.data;
            if (response.data.length < this.pageSize) {
              this.isLastPage = true;
            }
            this.loading = false;
          } else {
            this.error = 'Failed to load posts';
            this.loading = false;
          }
        },
        error: (error) => {
          console.error('Error loading posts:', error);
          this.error = 'Failed to load posts';
          this.loading = false;
        }
      });
  }

  private loadMore(): void {
    if (this.loadingMore || this.isLastPage) return;

    this.loadingMore = true;
    this.currentPage++;

    this.setupService.findAllByUserIdPageable(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.status === 200) {
            if (response.data.length > 0) {
              this.posts = [...this.posts, ...response.data];
              if (response.data.length < this.pageSize) {
                this.isLastPage = true;
              }
            } else {
              this.isLastPage = true;
            }
            this.loadingMore = false;
          } else {
            this.currentPage--;
            this.error = 'Failed to load more posts';
            this.loadingMore = false;
          }
        },
        error: (error) => {
          console.error('Error loading more posts:', error);
          this.currentPage--;
          this.error = 'Failed to load more posts';
          this.loadingMore = false;
        }
      });
  }
}
