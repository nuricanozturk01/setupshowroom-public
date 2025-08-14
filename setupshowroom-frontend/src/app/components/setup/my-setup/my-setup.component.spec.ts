import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MySetupComponent } from './my-setup.component';

describe('MySetupComponent', () => {
  let component: MySetupComponent;
  let fixture: ComponentFixture<MySetupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MySetupComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MySetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
