import {Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef} from '@angular/core';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {gravatarUrl} from '../../../utils/util';
import {ProfileService} from '../service/profile.service';
import {ApiResponse, ProfileInfo} from '../model/profile.model';
import {ToastService} from '../../shared/toast/toast.service';
import {SetupService} from '../../setup/service/setup.service';
import {SetupInfo} from '../../setup/setup-model';
import {Subject, takeUntil} from 'rxjs';
import { CommonModule } from '@angular/common';
import { ProductGroup, Product } from '../model/product.model';
import { SetupPostComponent } from '../../setup/setup-post/setup-post.component';

@Component({
  selector: 'app-public',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    SetupPostComponent
  ],
  templateUrl: './public.component.html',
  styleUrl: './public.component.scss'
})
export class PublicComponent implements OnInit, OnDestroy {
  @ViewChild('setupModal') setupModal!: ElementRef<HTMLDialogElement>;
  selectedSetup: SetupInfo | null = null;

  public loading: boolean = true;
  public profileInfo: ProfileInfo = {
    full_name: '',
    username: '',
    email: '',
    profession: '',
    profilePicture: '',
    system_info: {
      cpu: '',
      gpu: '',
      ram: '',
      storage: '',
      motherboard: '',
      psu: '',
      case: '',
      monitor: '',
      keyboard: '',
      mouse: '',
      headset: '',
      other: ''
    },
    product_groups: [],
    id: ''
  }

  public setups: SetupInfo[] = [];
  private setupCurrentPage = 0;
  private pageSize = 2;
  public loadingMore = false;
  public isLastPage = false;
  private destroy$ = new Subject<void>();

  // Group expansion
  expandedGroupId: string | null = null;

  // Group pagination
  currentPage = 1;
  itemsPerPage = 5;
  totalItems = 0;
  totalPages = 1;
  displayedGroups: ProductGroup[] = [];

  // Group products pagination
  groupProductsPerPage = 12;
  groupCurrentProductPages: { [key: string]: number } = {};
  groupTotalProductPages: { [key: string]: number } = {};
  groupDisplayedProducts: { [key: string]: Product[] } = {};

  constructor(
    private profileService: ProfileService,
    private setupService: SetupService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService) {
  }

  ngOnInit() {
    const username = this.route.snapshot.paramMap.get('username')!;
    if (!username) {
      this.loading = false;
      setTimeout(() => {
        this.router.navigate(['/explore']).then(() => {
          this.toastService.error('Username is required');
        })
      }, 500)
    } else {
      this.profileService.getUserProfile(username).subscribe({
        next: (data: ApiResponse<ProfileInfo>) => {
          this.loading = false;
          if (data.success && data.status === 200) {
            this.profileInfo = data.data;
            this.loadSetups();
            this.updateDisplayedGroups();
          } else {
            this.router.navigate(['/explore']).then(() => {
              this.toastService.error(data.message);
            })
          }
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.loading = false;
          this.router.navigate(['/explore']).then(() => {
            this.toastService.error('Failed to load profile');
          })
        }
      });
    }
  }

  ngOnDestroy() {
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
      this.loadMoreSetups();
    }
  }

  private loadSetups(): void {
    if (this.loadingMore) return;

    this.loadingMore = true;
    this.setupService.getSetupByUserId(this.profileInfo.id, this.setupCurrentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.status === 200) {
            this.setups = response.data;
            if (response.data.length < this.pageSize) {
              this.isLastPage = true;
            }
            this.loadingMore = false;
          } else {
            this.loadingMore = false;
          }
        },
        error: (error) => {
          console.error('Error loading setups:', error);
          this.loadingMore = false;
        }
      });
  }

  private loadMoreSetups(): void {
    if (this.loadingMore || this.isLastPage) return;

    this.loadingMore = true;
    this.setupCurrentPage++;

    this.setupService.getSetupByUserId(this.profileInfo.id, this.setupCurrentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response.success && response.status === 200) {
            if (response.data.length > 0) {
              this.setups = [...this.setups, ...response.data];
              if (response.data.length < this.pageSize) {
                this.isLastPage = true;
              }
            } else {
              this.isLastPage = true;
            }
            this.loadingMore = false;
          } else {
            this.setupCurrentPage--;
            this.loadingMore = false;
          }
        },
        error: (error) => {
          console.error('Error loading more setups:', error);
          this.setupCurrentPage--;
          this.loadingMore = false;
        }
      });
  }

  // Group methods
  toggleGroup(groupId: string) {
    this.expandedGroupId = this.expandedGroupId === groupId ? null : groupId;
    if (this.expandedGroupId === groupId) {
      // Reset to first page when expanding a group
      this.groupCurrentProductPages[groupId] = 1;
      this.updateGroupProducts(groupId);
    }
  }

  // Group pagination methods
  updateDisplayedGroups() {
    if (!this.profileInfo.product_groups) return;

    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedGroups = this.profileInfo.product_groups.slice(startIndex, endIndex);
    this.totalItems = this.profileInfo.product_groups.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.updateDisplayedGroups();
  }

  // Group products pagination methods
  updateGroupProducts(groupId: string) {
    const group = this.profileInfo.product_groups?.find(g => g.id === groupId);
    if (!group) return;

    const startIndex = ((this.groupCurrentProductPages[groupId] || 1) - 1) * this.groupProductsPerPage;
    const endIndex = startIndex + this.groupProductsPerPage;
    this.groupDisplayedProducts[groupId] = group.products.slice(startIndex, endIndex);
    this.groupTotalProductPages[groupId] = Math.ceil(group.products.length / this.groupProductsPerPage);
  }

  onGroupProductPageChange(groupId: string, page: number) {
    this.groupCurrentProductPages[groupId] = page;
    this.updateGroupProducts(groupId);
  }

  openSetupModal(setup: SetupInfo) {
    this.selectedSetup = setup;
    if (this.setupModal?.nativeElement) {
      this.setupModal.nativeElement.showModal();
    }
  }

  closeSetupModal() {
    this.selectedSetup = null;
    if (this.setupModal?.nativeElement) {
      this.setupModal.nativeElement.close();
    }
  }

  protected readonly gravatarUrl = gravatarUrl;
}
