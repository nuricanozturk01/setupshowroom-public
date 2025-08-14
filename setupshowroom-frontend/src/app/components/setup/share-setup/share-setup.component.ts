import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {SetupService} from '../service/setup.service';
import {ToastService} from '../../shared/toast/toast.service';
import {Router} from '@angular/router';
import {categories} from '../../../utils/category';
import imageCompression from 'browser-image-compression';

export interface Tag {
  id: string;
  name: string;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  description: string;
}

@Component({
  selector: 'app-share-setup',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './share-setup.component.html',
})
export class ShareSetupComponent {
  setupForm: FormGroup;
  imagePreviewUrls: string[] = [];
  videoPreviewUrls: string[] = [];
  selectedTags: Tag[] = [];
  tagInput = '';
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private setupService: SetupService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.setupForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      description: ['', [Validators.maxLength(1000)]],
      categories: [[], [Validators.required]],
      images: [null, [Validators.required]],
      videos: [null],
      tags: [[], [Validators.required, Validators.minLength(3), Validators.maxLength(15)]]
    });

  }

  isSelected(categoryId: string): boolean {
    return (this.setupForm.get('categories')?.value || []).includes(categoryId);
  }

  toggleCategory(categoryId: string) {
    const currentCategories = this.setupForm.get('categories')?.value || [];
    const index = currentCategories.indexOf(categoryId);

    if (index === -1) {
      currentCategories.push(categoryId);
    } else {
      currentCategories.splice(index, 1);
    }

    this.setupForm.patchValue({categories: currentCategories});
  }

  onImagesSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (this.imagePreviewUrls.length + files.length > 5) {
        this.toastService.warning('You can only upload up to 5 images');
        return;
      }

      files.forEach((file) => {
        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target?.result) {
            this.imagePreviewUrls.push(e.target.result as string);
          }
        };
        reader.readAsDataURL(file);
      });

      this.setupForm.patchValue({images: files});
    }
  }

  onVideosSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);
    const maxVideos = 1;
    const maxFileSizeMB = 30;
    const maxDurationSec = 60;

    const existingPreviewCount = this.videoPreviewUrls.length;
    const existingFiles = this.setupForm.value.videos || [];

    const remainingSlots = maxVideos - existingPreviewCount;

    if (remainingSlots <= 0) {
      this.toastService.warning(`You can only upload up to ${maxVideos} video`);
      return;
    }

    const validFiles: File[] = [];

    files.forEach((file) => {
      if (validFiles.length >= remainingSlots) return;

      const fileSizeMB = file.size / (1024 * 1024);
      if (fileSizeMB > maxFileSizeMB) {
        this.toastService.warning(`Video "${file.name}" exceeds the maximum size of ${maxFileSizeMB}MB and was not added.`);
        return;
      }

      const video = document.createElement('video');
      video.preload = 'metadata';
      video.onloadedmetadata = () => {
        window.URL.revokeObjectURL(video.src);
        const duration = video.duration;

        if (duration > maxDurationSec) {
          this.toastService.warning(`Video "${file.name}" is longer than ${maxDurationSec} seconds and was not added.`);
          return;
        }

        validFiles.push(file);

        const reader = new FileReader();
        reader.onload = (e) => {
          if (e.target?.result) {
            this.videoPreviewUrls.push(e.target.result as string);
          }
        };
        reader.readAsDataURL(file);

        const allFiles = [...existingFiles, ...validFiles].slice(0, maxVideos);
        this.setupForm.patchValue({videos: allFiles});
      };

      video.src = URL.createObjectURL(file);
    });
  }

  removeImage(index: number) {
    this.imagePreviewUrls.splice(index, 1);
    const currentFiles = this.setupForm.get('images')?.value || [];
    const updatedFiles = currentFiles.filter((_: any, i: number) => i !== index);
    this.setupForm.patchValue({images: updatedFiles});
  }

  removeVideo(index: number) {
    this.videoPreviewUrls.splice(index, 1);
    const currentFiles = this.setupForm.get('videos')?.value || [];
    const updatedFiles = currentFiles.filter((_: any, i: number) => i !== index);
    this.setupForm.patchValue({videos: updatedFiles});
  }

  addTag() {
    if (this.tagInput.trim()) {
      if (this.selectedTags.length >= 15) {
        this.toastService.warning('Maximum 15 tags allowed');
        return;
      }
      const newTag: Tag = {
        id: Date.now().toString(),
        name: this.tagInput.trim(),
      };
      this.selectedTags.push(newTag);
      this.updateTagsFormControl();
      this.tagInput = '';
    }
  }

  removeTag(tag: Tag) {
    this.selectedTags = this.selectedTags.filter((t) => t.id !== tag.id);
    this.updateTagsFormControl();
  }

  private updateTagsFormControl() {
    const tagNames = this.selectedTags.map(tag => tag.name);
    this.setupForm.patchValue({
      tags: tagNames
    }, { emitEvent: false });
  }

  isFormValid(): boolean {
    const hasValidImages = this.imagePreviewUrls.length > 0;
    const hasValidVideos = this.videoPreviewUrls.length <= 1;
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

    const setupData = {
      title: this.setupForm.value.title,
      description: this.setupForm.value.description,
      categories: this.setupForm.value.categories,
      tags: this.selectedTags.map((tag) => tag.name),
    };

    formData.append('setup', new Blob([JSON.stringify(setupData)], {type: 'application/json'}));

    const images: File[] = this.setupForm.get('images')?.value || [];
    for (const file of images) {
      try {
        const resizedImage = await imageCompression(file, {
          maxWidthOrHeight: 1080,
          maxSizeMB: 1,
          useWebWorker: true,
        });
        formData.append('images', resizedImage, file.name);
      } catch (error) {
        console.error('Image compression error:', error);
      }
    }

    if (images.length < 1) {
      this.toastService.warning('You must upload at least 1 image');
      this.isSubmitting = false;
      return;
    }


    // @ts-ignore
    const videos: File[] = this.setupForm.get('videos').value;
    if (videos) {
      videos.forEach((file: File) => {
        formData.append('videos', file, file.name);
      });
    }

    this.setupService.createSetup(formData).subscribe({
      next: (response) => {
        if (response.success && response.status === 200) {
          this.toastService.success('Setup created successfully');
          setTimeout(() => {
            this.router.navigate(['/my-setups']).then((r) => {
              if (!r) {
                this.toastService.error('Failed to redirect to my setups');
              }
            });
          }, 2000);
        }
      },
      error: (error) => {
        if (error.status === 400) {
          if (error.error.data) {
            this.toastService.warning(error.error.data);
            this.isSubmitting = false;
          } else {
            this.toastService.error('Failed to create setup: ' + (error.error?.message || 'Unknown error'));
            this.isSubmitting = false;
          }
        } else {
          this.toastService.error(error.message);
          this.isSubmitting = false;
        }
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  protected readonly categories = categories;
}
