import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {
  ApiResponse,
  ProfileInfo,
  UserProfileForm,
  SystemSpecs,
  UserInfo
} from './model/profile.model';
import {
  ProductGroup,
  Product,
  CreateProductRequest,
  CreateProductGroupRequest,
  UpdateProductGroupRequest
} from './model/product.model';
import {ProfileService} from './service/profile.service';
import {ProductService} from '../favorite/service/product.service';
import md5 from 'blueimp-md5';
import {ToastService} from '../shared/toast/toast.service';
import {categories} from '../../utils/category';
import {Router} from '@angular/router';
import {SetupService} from '../setup/service/setup.service';
import imageCompression from 'browser-image-compression';

const SYSTEM_SPEC_MAX_CHARACTERS = 30;
const ADDITIONAL_INFO_MAX_CHARACTERS = 300;

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
})
export class ProfileComponent implements OnInit {
  @ViewChild('deleteGroupModal') deleteGroupModal!: ElementRef;
  @ViewChild('deleteProductModal') deleteProductModal!: ElementRef;
  @ViewChild('cardImagesModal') cardImagesModal!: ElementRef;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  profileForm: UserProfileForm = {
    username: '',
    full_name: '',
    email: '',
    profession: '',
  };

  passwordForm: FormGroup;

  systemSpecs: SystemSpecs = {
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
  };

  isAddingGroup = false;
  productGroups: ProductGroup[] = [];
  expandedGroupId: string | null = null;
  addingProductToGroup: string | null = null;
  newGroupName = '';
  newProduct: CreateProductRequest = {
    name: '',
    url: '',
    group_id: ''
  };
  editingGroupId: string | null = null;
  editingGroupName = '';
  editingProductId: string | null = null;
  editingProduct: CreateProductRequest = {
    name: '',
    url: '',
    group_id: ''
  };

  // Modal state
  groupToDelete: string | null = null;
  productToDelete: { groupId: string; productId: string } | null = null;

  // Pagination properties
  currentPage = 1;
  itemsPerPage = 5;
  totalItems = 0;
  totalPages = 1;
  displayedGroups: ProductGroup[] = [];

  // Product pagination properties
  productsPerPage = 10;
  currentProductPages: { [key: string]: number } = {};
  totalProductPages: { [key: string]: number } = {};
  displayedProducts: { [key: string]: Product[] } = {};

  // Image upload properties
  public existingImageUrls: string[] = [];
  public newImageFiles: File[] = [];
  public imagePreviewUrls: string[] = [];
  public maxImages = 5;

  // Categories
  public selectedCategories: string[] = [];
  public availableCategories = categories;

  systemSpecsForm: FormGroup;

  profile: ProfileInfo = {} as ProfileInfo;

  cardImages: string[] = [];
  selectedCardImage: string | null = null;
  copiedImages: { [key: string]: boolean } = {};

  constructor(
    private profileService: ProfileService,
    private productService: ProductService,
    public toastService: ToastService,
    private router: Router,
    private fb: FormBuilder,
    private setupService: SetupService
  ) {
    this.passwordForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
    }, {validator: this.passwordMatchValidator});

    this.systemSpecsForm = this.fb.group({
      cpu: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      gpu: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      ram: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      storage: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      motherboard: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      psu: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      case: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      monitor: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      keyboard: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      mouse: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      headset: ['', [Validators.maxLength(SYSTEM_SPEC_MAX_CHARACTERS)]],
      other: ['', [Validators.maxLength(50)]]
    });
  }

  ngOnInit() {
    this.loadProfile();
  }

  // Group Operations
  startAddingGroup() {
    this.isAddingGroup = true;
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null
      : {'mismatch': true};
  }

  changePassword() {
    if (this.passwordForm.valid) {
      const passwordForm = {
        new_password: this.passwordForm.get('newPassword')?.value
      }
      this.profileService.changePassword(passwordForm).subscribe({
        next: (response) => {
          if (response.success) {
            this.toastService.success('Password changed successfully');
            this.passwordForm.reset();
          } else {
            this.toastService.error('Failed to change password');
          }
        },
        error: (error) => {
          if (error.error.text === "invalidPassword") {
            this.toastService.error('Current password is incorrect');
          } else {
            this.toastService.error('Failed to change password');
          }
        }
      });
    }
  }

  createGroup() {
    if (!this.newGroupName.trim()) {
      this.toastService.error('Group name cannot be empty');
      return;
    }

    if (this.newGroupName.length < 5) {
      this.toastService.error('Group name must be at least 5 characters');
      return;
    }

    if (this.newGroupName.length > 20) {
      this.toastService.error('Group name must be at most 20 characters');
      return;
    }

    const request: CreateProductGroupRequest = {name: this.newGroupName.trim()};

    this.productService.createProductGroup(request).subscribe({
      next: (response) => {
        if (response.success) {
          this.productGroups.push(response.data);
          this.updateDisplayedGroups();
          this.toastService.success('Group created successfully');
          this.isAddingGroup = false;
          this.newGroupName = '';
        } else {
          this.toastService.error('Failed to create group');
        }
      },
      error: () => this.toastService.error('Failed to create group')
    });
  }

  deleteGroup(groupId: string) {
    this.productService.deleteProductGroup(groupId).subscribe({
      next: (response) => {
        if (response.success) {
          this.productGroups = this.productGroups.filter(group => group.id !== groupId);
          this.updateDisplayedGroups();
          this.toastService.success('Product Group deleted successfully');
        } else {
          this.toastService.error('Failed to delete group');
        }
      },
      error: () => this.toastService.error('Failed to delete group')
    });
  }

  toggleGroup(groupId: string) {
    this.expandedGroupId = this.expandedGroupId === groupId ? null : groupId;
    if (this.expandedGroupId === groupId) {
      if (!this.currentProductPages[groupId]) {
        this.currentProductPages[groupId] = 1;
      }
      this.updateDisplayedProducts(groupId);
    }
  }

  startEditGroup(group: ProductGroup) {
    this.editingGroupId = group.id;
    this.editingGroupName = group.name;
  }

  cancelEditGroup() {
    this.editingGroupId = null;
    this.editingGroupName = '';
  }

  // Product methods
  startAddProduct(groupId: string) {
    this.addingProductToGroup = groupId;
    this.newProduct = {name: '', url: '', group_id: groupId};
  }

  cancelAddProduct() {
    this.addingProductToGroup = null;
    this.newProduct = {name: '', url: '', group_id: ''};
  }

  isValidUrl(url: string): boolean {
    try {
      new URL(url);
      return true;
    } catch {
      return false;
    }
  }

  addProduct(groupId: string) {
    if (!this.newProduct.name.trim()) {
      this.toastService.error('Product name cannot be empty');
      return;
    }

    if (this.newProduct.name.length < 5) {
      this.toastService.error('Product name must be at least 5 characters');
      return;
    }

    if (this.newProduct.name.length > 20) {
      this.toastService.error('Product name must be at most 20 characters');
      return;
    }

    if (!this.newProduct.url) {
      this.toastService.error('Product URL cannot be empty');
      return;
    }

    try {
      const url = new URL(this.newProduct.url);
      if (!url.protocol.startsWith('http')) {
        throw new Error('Invalid protocol');
      }
    } catch {
      this.toastService.error('Please enter a valid URL starting with http:// or https://');
      return;
    }

    const request: CreateProductRequest = {
      name: this.newProduct.name.trim(),
      url: this.newProduct.url.trim(),
      group_id: groupId
    };

    this.productService.createProduct(groupId, request).subscribe({
      next: (response) => {
        if (response.success) {
          const group = this.productGroups.find(g => g.id === groupId);
          if (group) {
            group.products.unshift(response.data);
            this.currentProductPages[groupId] = 1;
            this.updateDisplayedProducts(groupId);
          }
          this.toastService.success('Product added successfully');
          this.cancelAddProduct();
        } else {
          this.toastService.error('Failed to add product');
        }
      },
      error: () => this.toastService.error('Failed to add product')
    });
  }

  startEditProduct(product: Product) {
    this.editingProductId = product.id;
    this.editingProduct = {
      name: product.name,
      url: product.url,
      group_id: ''
    };
  }

  cancelEditProduct() {
    this.editingProductId = null;
    this.editingProduct = {name: '', url: '', group_id: ''};
  }

  deleteProduct(groupId: string, productId: string) {
    this.productService.deleteProduct(productId, groupId).subscribe({
      next: (response) => {
        if (response.success) {
          const group = this.productGroups.find(g => g.id === groupId);
          if (group) {
            group.products = group.products.filter(p => p.id !== productId);
            // Update current page if it would be empty
            const totalPages = Math.ceil(group.products.length / this.productsPerPage);
            if (this.currentProductPages[groupId] > totalPages) {
              this.currentProductPages[groupId] = Math.max(1, totalPages);
            }
            this.updateDisplayedProducts(groupId);
          }
          this.toastService.success('Product deleted successfully');
        } else {
          this.toastService.error('Failed to delete product');
        }
      },
      error: () => this.toastService.error('Failed to delete product')
    });
  }

  loadProfile() {
    this.profileService.getProfile().subscribe((data: ApiResponse<ProfileInfo>) => {
      if (data.success && data.status == 200) {
        this.profileToForm(data.data);
        this.updateDisplayedGroups();
        // Load images and categories
        if (data.data.system_info?.images) {
          this.existingImageUrls = data.data.system_info.images;
        }
        if (data.data.system_info?.categories) {
          this.selectedCategories = data.data.system_info.categories;
        }
      } else {
        this.toastService.error('Failed to load profile');
      }
    });
  }

  private profileToForm(profileInfo: ProfileInfo): void {
    this.profileForm = {
      username: profileInfo.username!!,
      full_name: profileInfo.full_name!!,
      email: profileInfo.email!!,
      profession: profileInfo.profession,
    };

    if (profileInfo.system_info !== null) {
      this.systemSpecs = profileInfo.system_info!!;
    }

    if (profileInfo.product_groups !== null) {
      this.productGroups = profileInfo.product_groups!!;
    }
  }

  saveProfile(): void {
    this.profileService.updateProfile(this.profileForm).subscribe((data: ApiResponse<UserInfo>) => {
      if (data.success) {
        const profileInfo = data.data;
        this.profileForm = {
          username: profileInfo.username!!,
          full_name: profileInfo.full_name!!,
          email: profileInfo.email!!,
          profession: profileInfo.profession,
        };
        this.toastService.success('User saved successfully');
        setTimeout(() => {
          window.location.reload();
        })
      } else {
        this.toastService.error('Failed to save user');
      }
    }, error => {
      console.error(error);
      if (error.error.text === "usernameAlreadyExists") {
        this.toastService.warning('Username or email already exists');
      }

      if (error.error.data === "emailCannotChange") {
        this.toastService.warning(error.error.text);
      }
    });
  }

  async saveSystemInfo(): Promise<void> {
    const formData = new FormData();

    const updatedSystemSpecs = {
      ...this.systemSpecs,
      categories: this.selectedCategories,
      existing_images: this.existingImageUrls
    };

    formData.append('system_specs', new Blob([JSON.stringify(updatedSystemSpecs)], {type: 'application/json'}));

    if (this.newImageFiles && this.newImageFiles.length > 0) {
      for (const file of this.newImageFiles) {
        try {
          const resizedImage = await imageCompression(file, {
            maxWidthOrHeight: 1080,
            maxSizeMB: 1,
            useWebWorker: true,
          });
          formData.append('images', resizedImage, file.name);
        } catch (error) {
          console.error('Image compression error:', error);
          formData.append('images', file, file.name);
        }
      }
    }

    this.profileService.upsertSystemSpecs(formData).subscribe((data: ApiResponse<ProfileInfo>) => {
      if (data.success && data.status == 200) {
        // Update system specs and existing images from the response
        if (data.data.system_info) {
          this.systemSpecs = data.data.system_info;
          if (data.data.system_info.images) {
            this.existingImageUrls = data.data.system_info.images;
          }
        }
        this.toastService.success("System specs saved successfully");
        // Reset new image arrays after successful save
        this.newImageFiles = [];
        this.imagePreviewUrls = [];
      } else {
        console.log(data);
      }
    }, error => {
        if (error.status === 400) {
          if (error.error.data) {
            this.toastService.warning(error.error.data);
          } else {
            this.toastService.error('Failed to save system specs');
          }
        }
      });
  }

  gravatarUrl(email: string): string {
    const emailHash = md5(email.trim().toLowerCase());
    return `https://www.gravatar.com/avatar/${emailHash}?s=150&d=identicon`;
  }

  updateGroupName(groupId: string) {
    if (!this.editingGroupName.trim()) {
      this.toastService.error('Group name cannot be empty');
      return;
    }

    if (this.editingGroupName.length < 5) {
      this.toastService.error('Group name must be at least 5 characters');
      return;
    }

    if (this.editingGroupName.length > 20) {
      this.toastService.error('Group name must be at most 20 characters');
      return;
    }

    const request: UpdateProductGroupRequest = {name: this.editingGroupName.trim()};
    this.productService.updateProductGroup(groupId, request).subscribe({
      next: (response) => {
        if (response.success) {
          const group = this.productGroups.find(g => g.id === groupId);
          if (group) {
            group.name = this.editingGroupName.trim();
          }
          this.toastService.success('Group name updated successfully');
          this.cancelEditGroup();
        } else {
          this.toastService.error('Failed to update group name');
        }
      },
      error: () => this.toastService.error('Failed to update group name')
    });
  }

  updateProduct(groupId: string, productId: string) {
    if (!this.editingProduct.name.trim()) {
      this.toastService.error('Product name cannot be empty');
      return;
    }

    if (this.editingProduct.name.length < 5) {
      this.toastService.error('Product name must be at least 5 characters');
      return;
    }

    if (this.editingProduct.name.length > 20) {
      this.toastService.error('Product name must be at most 20 characters');
      return;
    }

    if (!this.editingProduct.url) {
      this.toastService.error('Product URL cannot be empty');
      return;
    }

    try {
      const url = new URL(this.editingProduct.url);
      if (!url.protocol.startsWith('http')) {
        throw new Error('Invalid protocol');
      }
    } catch {
      this.toastService.error('Please enter a valid URL starting with http:// or https://');
      return;
    }

    this.productService.updateProduct(productId, groupId, this.editingProduct).subscribe({
      next: (response) => {
        if (response.success) {
          // update product
          const group = this.productGroups.find(g => g.id === groupId);
          if (group) {
            const product = group.products.find(p => p.id === productId);
            if (product) {
              product.name = this.editingProduct.name.trim();
              product.url = this.editingProduct.url.trim();
            }
          }
          this.toastService.success('Product updated successfully');
          this.cancelEditProduct();
        } else {
          this.toastService.error('Failed to update product');
        }
      },
      error: () => this.toastService.error('Failed to update product')
    });
  }

  // Modal methods
  openDeleteGroupModal(groupId: string) {
    this.groupToDelete = groupId;
    this.deleteGroupModal.nativeElement.showModal();
  }

  closeDeleteGroupModal() {
    this.deleteGroupModal.nativeElement.close();
    this.groupToDelete = null;
  }

  confirmDeleteGroup() {
    if (this.groupToDelete) {
      this.deleteGroup(this.groupToDelete);
      this.closeDeleteGroupModal();
    }
  }

  // Product methods
  openDeleteProductModal(groupId: string, productId: string) {
    this.productToDelete = {groupId, productId};
    this.deleteProductModal.nativeElement.showModal();
  }

  closeDeleteProductModal() {
    this.deleteProductModal.nativeElement.close();
    this.productToDelete = null;
  }

  confirmDeleteProduct() {
    if (this.productToDelete) {
      this.deleteProduct(this.productToDelete.groupId, this.productToDelete.productId);
      this.closeDeleteProductModal();
    }
  }

  // Pagination methods
  updateDisplayedGroups() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedGroups = this.productGroups.slice(startIndex, endIndex);
    this.totalItems = this.productGroups.length;
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.updateDisplayedGroups();
  }

  // Product pagination methods
  updateDisplayedProducts(groupId: string) {
    const group = this.productGroups.find(g => g.id === groupId);
    if (!group) return;

    const startIndex = ((this.currentProductPages[groupId] || 1) - 1) * this.productsPerPage;
    const endIndex = startIndex + this.productsPerPage;
    this.displayedProducts[groupId] = group.products.slice(startIndex, endIndex);
    this.totalProductPages[groupId] = Math.ceil(group.products.length / this.productsPerPage);
  }

  onProductPageChange(groupId: string, page: number) {
    this.currentProductPages[groupId] = page;
    this.updateDisplayedProducts(groupId);
  }

  // Image upload methods
  onImagesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (this.existingImageUrls.length + this.newImageFiles.length + files.length > this.maxImages) {
        this.toastService.error(`You can only upload up to ${this.maxImages} images`);
        return;
      }

      files.forEach(file => {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target?.result) {
            this.imagePreviewUrls.push(e.target.result as string);
          }
        };
        reader.readAsDataURL(file);
        this.newImageFiles.push(file);
      });
    }
  }

  removeExistingImage(index: number) {
    this.existingImageUrls.splice(index, 1);
  }

  removeNewImage(index: number) {
    this.imagePreviewUrls.splice(index, 1);
    this.newImageFiles.splice(index, 1);
  }

  // Category methods
  isSelected(categoryId: string): boolean {
    return this.selectedCategories.includes(categoryId);
  }

  toggleCategory(categoryId: string) {
    const index = this.selectedCategories.indexOf(categoryId);
    if (index === -1) {
      this.selectedCategories.push(categoryId);
    } else {
      this.selectedCategories.splice(index, 1);
    }
  }

  async createSetupFromSpecs(): Promise<void> {
    // Validate if we have categories and images
    if (this.selectedCategories.length === 0) {
      this.toastService.error('Please select at least one category');
      return;
    }

    if (this.existingImageUrls.length === 0 && this.newImageFiles.length === 0) {
      this.toastService.error('Please add at least one image');
      return;
    }

    try {
      // Create tags from system specs
      const tags: string[] = [];
      if (this.systemSpecs.cpu) tags.push(`${this.systemSpecs.cpu}`);
      if (this.systemSpecs.gpu) tags.push(`${this.systemSpecs.gpu}`);
      if (this.systemSpecs.ram) tags.push(`${this.systemSpecs.ram}`);
      if (this.systemSpecs.storage) tags.push(`${this.systemSpecs.storage}`);
      if (this.systemSpecs.motherboard) tags.push(`${this.systemSpecs.motherboard}`);
      if (this.systemSpecs.psu) tags.push(`${this.systemSpecs.psu}`);
      if (this.systemSpecs.case) tags.push(`${this.systemSpecs.case}`);
      if (this.systemSpecs.monitor) tags.push(`${this.systemSpecs.monitor}`);
      if (this.systemSpecs.keyboard) tags.push(`${this.systemSpecs.keyboard}`);
      if (this.systemSpecs.mouse) tags.push(`${this.systemSpecs.mouse}`);
      if (this.systemSpecs.headset) tags.push(`${this.systemSpecs.headset}`);

      // Create FormData for the setup
      const formData = new FormData();

      // Create setup data
      const setupData = {
        title: `${this.profileForm.username}'s Setup`,
        description: 'This is a setup created from system specs',
        categories: this.selectedCategories,
        tags: tags,
        existing_images: this.existingImageUrls,
      };

      formData.append('setup', new Blob([JSON.stringify(setupData)], {type: 'application/json'}));

      // Add new images if any with compression
      if (this.newImageFiles.length > 0) {
        for (const file of this.newImageFiles) {
          try {
            const resizedImage = await imageCompression(file, {
              maxWidthOrHeight: 1080,
              maxSizeMB: 1,
              useWebWorker: true,
            });
            formData.append('images', resizedImage, file.name);
          } catch (error) {
            console.error('Image compression error:', error);
            formData.append('images', file, file.name);
          }
        }
      }

      // Create setup using SetupService
      this.setupService.createSetupByUserProfile(formData).subscribe({
        next: (response) => {
          if (response.success && response.status === 200) {
            this.toastService.success('Setup created successfully');
            // Navigate to my-setups page after successful creation
            setTimeout(() => {
              this.router.navigate(['/my-setups']).then(r => {
                if (!r) {
                  this.toastService.error('Failed to redirect to my setups');
                }
              });
            }, 2000);
          }
        },
        error: (error) => {
          this.toastService.error('Failed to create setup: ' + error.message);
        }
      });
    } catch (error) {
      this.toastService.error('Failed to process images: ' + (error as Error).message);
    }
  }

  showSetupCards() {
    if (!this.selectedCategories || this.selectedCategories.length === 0) {
      this.toastService.warning('Please complete your system info');
      return;
    }

    this.setupService.getCardImages().subscribe({
      next: (response) => {
        if (response.success) {
          this.cardImages = response.data;
          this.cardImagesModal.nativeElement.showModal();
        } else {
          this.toastService.error('Failed to load card images');
        }
      },
      error: () => {
        this.toastService.error('Failed to load card images');
      }
    });
  }

  closeCardImagesModal() {
    this.cardImagesModal.nativeElement.close();
    this.selectedCardImage = null;
  }

  copyCardImage(imgHtml: string) {
    navigator.clipboard.writeText(imgHtml).then(() => {
      this.copiedImages[imgHtml] = true;
      setTimeout(() => {
        this.copiedImages[imgHtml] = false;
      }, 2000);
    }).catch(() => {
      this.toastService.error('Failed to copy image code');
    });
  }

  extractImageUrl(imgHtml: string): string {
    const match = imgHtml.match(/src="([^"]+)"/);
    return match ? match[1] : '';
  }

  protected readonly MAX_CHARACTERS = SYSTEM_SPEC_MAX_CHARACTERS;
  protected readonly ADDITIONAL_INFO_MAX_CHARACTERS = ADDITIONAL_INFO_MAX_CHARACTERS;

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }
}
