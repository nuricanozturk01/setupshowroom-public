import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {SetupService} from '../service/setup.service';
import {SetupInfo, SetupUpdateForm} from '../setup-model';
import {ApiResponse} from '../../profile/model/profile.model';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Category} from '../share-setup/share-setup.component';
import {ToastService} from '../../shared/toast/toast.service';
import {ChangeDetectorRef} from '@angular/core';
import {categories} from '../../../utils/category';
import imageCompression from 'browser-image-compression';

@Component({
  selector: 'app-setup-update',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './setup-update.component.html',
  styleUrl: './setup-update.component.scss'
})
export class SetupUpdateComponent implements OnInit {
  loading: boolean = true;
  setupForm: FormGroup;
  existingImageUrls: string[] = [];
  existingVideoUrls: string[] = [];
  newImageFiles: File[] = [];
  newVideoFiles: File[] = [];
  imagePreviewUrls: string[] = [];
  videoPreviewUrls: string[] = [];
  selectedTags: any[] = [];
  tagInput = '';
  isSubmitting = false;
  setupId!: string;

  categories: Category[] = categories;

  constructor(
    private fb: FormBuilder,
    private setupService: SetupService,
    private route: ActivatedRoute,
    private router: Router,
    private toastService: ToastService,
    private cdr: ChangeDetectorRef
  ) {
    this.setupForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(1000)]],
      categories: [[], [Validators.required]],
      tags: [[], [Validators.required, Validators.minLength(3), Validators.maxLength(15)]]
    });
  }

  ngOnInit() {
    this.setupId = this.route.snapshot.paramMap.get('id')!;
    this.loadSetup();
  }

  loadSetup() {
    this.setupService.getSetupById(this.setupId).subscribe((response: ApiResponse<SetupInfo>) => {
        if (response.success) {
          this.loading = false;
          const setup = response.data;

          this.setupForm.patchValue({
            title: setup.title,
            description: setup.description,
            categories: setup.categories,
            tags: setup.tags
          });

          this.selectedTags = setup.tags.map(tag => ({name: tag}));
          this.existingImageUrls = setup.images;
          this.existingVideoUrls = setup.videos;
          this.cdr.detectChanges();
        } else {
          this.loading = false;
          this.toastService.error('Failed to load setup. Navigate to my setups');
          setTimeout(() => {
            this.router.navigate(['/my-setups']);
          }, 700);
        }
      },
      error => {
        this.loading = false;
        this.toastService.error('Failed to load setup. Navigate to my setups');
        setTimeout(() => {
          this.router.navigate(['/my-setups']);
        }, 700);
      });
  }

  isSelected(categoryId: string): boolean {
    return this.setupForm.value.categories.includes(categoryId);
  }

  toggleCategory(categoryId: string) {
    const categories = this.setupForm.value.categories;
    const index = categories.indexOf(categoryId);
    index === -1 ? categories.push(categoryId) : categories.splice(index, 1);
    this.setupForm.patchValue({categories});
  }

  onImagesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (this.imagePreviewUrls.length + files.length > 5) {
        this.toastService.warning('You can only upload up to 5 images');
        return;
      }
      Array.from(input.files).forEach(file => {
        const reader = new FileReader();
        reader.onload = e => this.imagePreviewUrls.push(e.target!.result as string);
        reader.readAsDataURL(file);
        this.newImageFiles.push(file);
      });
    }
  }

  onVideosSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);
    const maxVideos = 1;
    const maxFileSizeMB = 30;
    const maxDurationSec = 60;

    const totalVideos = this.existingVideoUrls.length + this.newVideoFiles.length;
    const remainingSlots = maxVideos - totalVideos;

    if (remainingSlots <= 0) {
      this.toastService.warning(`You can only upload up to ${maxVideos} video`);
      return;
    }

    files.forEach((file) => {
      if (this.newVideoFiles.length >= remainingSlots) {
        this.toastService.warning(`Maximum ${maxVideos} video allowed`);
        return;
      }

      const fileSizeMB = file.size / (1024 * 1024);
      if (fileSizeMB > maxFileSizeMB) {
        this.toastService.warning(`Video "${file.name}" exceeds the maximum size of ${maxFileSizeMB}MB`);
        return;
      }

      const video = document.createElement('video');
      video.preload = 'metadata';

      video.onloadedmetadata = () => {
        window.URL.revokeObjectURL(video.src);
        const duration = video.duration;

        if (duration > maxDurationSec) {
          this.toastService.warning(`Video "${file.name}" is longer than ${maxDurationSec} seconds`);
          return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target?.result) {
            this.videoPreviewUrls.push(e.target.result as string);
            this.newVideoFiles.push(file);
            this.cdr.detectChanges();
          }
        };
        reader.readAsDataURL(file);
      };

      video.src = URL.createObjectURL(file);
    });
  }

  removeExistingImage(index: number) {
    this.existingImageUrls.splice(index, 1);
  }

  removeExistingVideo(index: number) {
    this.existingVideoUrls.splice(index, 1);
  }

  removeNewImage(index: number) {
    this.imagePreviewUrls.splice(index, 1);
    this.newImageFiles.splice(index, 1);
  }

  removeNewVideo(index: number) {
    this.videoPreviewUrls.splice(index, 1);
    this.newVideoFiles.splice(index, 1);
  }

  addTag() {
    if (this.tagInput.trim()) {
      if (this.selectedTags.length >= 15) {
        this.toastService.warning('Maximum 15 tags allowed');
        return;
      }
      this.selectedTags.push({name: this.tagInput.trim()});
      this.tagInput = '';
      this.updateTagsFormControl();
      this.cdr.detectChanges();
    }
  }

  removeTag(tag: any) {
    this.selectedTags = this.selectedTags.filter(t => t !== tag);
    this.updateTagsFormControl();
    this.cdr.detectChanges();
  }

  private updateTagsFormControl() {
    const tagNames = this.selectedTags.map(tag => tag.name);
    this.setupForm.patchValue({
      tags: tagNames
    }, {emitEvent: false});
  }

  isFormValid(): boolean {
    const hasValidImages = this.existingImageUrls.length + this.newImageFiles.length > 0;
    const hasValidVideos = this.existingVideoUrls.length + this.newVideoFiles.length <= 1;
    const hasValidTags = this.selectedTags.length >= 3 && this.selectedTags.length <= 15;
    const categoriesControl = this.setupForm.get('categories');
    const titleControl = this.setupForm.get('title');
    const hasValidCategories = categoriesControl?.value?.length > 0 || false;
    const hasValidTitle = titleControl?.valid || false;

    return hasValidImages &&
      hasValidVideos &&
      hasValidTags &&
      hasValidCategories &&
      hasValidTitle;
  }

  async onSubmit() {
    if (!this.isFormValid()) {
      this.toastService.warning('Please fill all required fields correctly');
      return;
    }

    this.isSubmitting = true;
    const formData = new FormData();

    const setupData: SetupUpdateForm = {
      title: this.setupForm.value.title,
      description: this.setupForm.value.description,
      categories: this.setupForm.value.categories,
      tags: this.selectedTags.map(t => t.name),
      existing_images: this.existingImageUrls,
      existing_videos: this.existingVideoUrls
    };

    formData.append('setup', new Blob([JSON.stringify(setupData)], {type: 'application/json'}));

    // Add new images with compression
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

    // Add new videos
    this.newVideoFiles.forEach(file => formData.append('videos', file));

    this.setupService.updateSetup(this.setupId, formData, setupData).subscribe({
      next: (response) => {
        if (response.success && response.status === 200) {
          this.toastService.success('Setup updated successfully');
          setTimeout(() => this.router.navigate(["/my-setups"]), 1000);
        } else {
          this.toastService.error('Failed to update setup');
          this.isSubmitting = false;
        }
      },
      error: (err) => {
        if (err.status === 400) {
          if (err.error.data) {
            this.toastService.warning(err.error.data);
            this.isSubmitting = false;
          } else {
            this.toastService.error('Failed to update setup: ' + (err.error?.message || 'Unknown error'));
            this.isSubmitting = false;
          }
        } else {
          this.toastService.error('Failed to update setup: ' + (err.error?.message || 'Unknown error'));
          console.error(err);
          this.isSubmitting = false;
        }
      }
    });
  }
}
