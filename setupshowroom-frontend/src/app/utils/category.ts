import {Category} from '../components/setup/share-setup/share-setup.component';

export const categories: Category[] = [
  {
    id: 'GAMING',
    name: 'Gaming',
    icon: 'fas fa-gamepad',
    description: 'Gaming and streaming'
  },
  {
    id: 'PRODUCTIVITY',
    name: 'Productivity',
    icon: 'fas fa-briefcase',
    description: 'Work and productivity'
  },
  {
    id: 'DEVELOPMENT',
    name: 'Developer',
    icon: 'fas fa-code',
    description: 'Software development'
  },
  {
    id: 'PC',
    name: 'PC',
    icon: 'fas fa-video',
    description: 'Personal computer setup'
  },
  {
    id: 'MINIMALIST',
    name: 'Minimalist',
    icon: 'fas fa-minus',
    description: 'Clean and minimal designs'
  },
  {
    id: 'RGB',
    name: 'RGB',
    icon: 'fas fa-palette',
    description: 'RGB and lighting focused'
  },
  {
    id: 'WORKSPACE',
    name: 'Workspace',
    icon: 'fas fa-home',
    description: 'Professional home workspace'
  },
  {
    id: 'ROOM',
    name: 'Room',
    icon: 'fas fa-graduation-cap',
    description: 'Room and dorm setups'
  }
];


export const categoryNames = categories.map(category => category.id);
