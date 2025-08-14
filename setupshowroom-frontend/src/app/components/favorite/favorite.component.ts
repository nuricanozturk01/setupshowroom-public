import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";
import {SetupPostComponent} from "../setup/setup-post/setup-post.component";
import {SetupInfo} from '../setup/setup-model';
import {Subject, takeUntil} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {ExploreService} from '../explore/service/explore.service';
import {FavoriteService} from './favorite.service';

@Component({
  selector: 'app-favorite',
  standalone: true,
    imports: [
        NgForOf,
        NgIf,
        SetupPostComponent
    ],
  templateUrl: './favorite.component.html',
  styleUrl: './favorite.component.scss'
})
export class FavoriteComponent  implements OnInit, OnDestroy {
  public posts: SetupInfo[] = [];
  public loading = false;
  public error: string | null = null;
  public currentPage = 0;
  public isLastPage = false;
  private destroy$ = new Subject<void>();
  private loadingMore = false;
  private readonly pageSize = 2; // Fixed page size

  constructor(
    private route: ActivatedRoute,
    private favoriteService: FavoriteService
  ) {}

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

    this.favoriteService.getFeedPageable(this.currentPage, this.pageSize)
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

    this.favoriteService.getFeedPageable(this.currentPage, this.pageSize)
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
