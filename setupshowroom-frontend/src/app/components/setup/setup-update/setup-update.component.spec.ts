import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SetupUpdateComponent } from './setup-update.component';

describe('SetupUpdateComponent', () => {
  let component: SetupUpdateComponent;
  let fixture: ComponentFixture<SetupUpdateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SetupUpdateComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SetupUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
