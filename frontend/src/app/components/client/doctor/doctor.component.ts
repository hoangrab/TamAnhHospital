import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router, RouterLinkActive } from '@angular/router';
import { Observable } from 'rxjs';
import { Doctor } from 'src/app/models/doctor';
import { Major } from 'src/app/models/major';
import { DoctorService } from 'src/app/services/doctor.service';
import { MajorService } from 'src/app/services/major.service';

@Component({
  selector: 'app-doctor',
  templateUrl: './doctor.component.html',
  styleUrls: ['./doctor.component.scss']
})
export class DoctorComponent {
  // render, search, select major, pagination
  listDoctor!: Observable<Doctor[]>
  listMajor!: Observable<Major[]>
  addForm !: FormGroup;
  khoa = 0;

  constructor(private route : Router, private doctorsv : DoctorService, private majorsv :MajorService,
    private formbuilder : FormBuilder){};

  ngOnInit() {
    this.addForm = this.formbuilder.group({
      key : ['']
    })
    this.listDoctor = this.doctorsv.getAllDoctors('true');
    this.listDoctor.subscribe({
      next(value) {
          // logic code
          console.log('get data ok' + value)
      },
      error(err) {
          console.log('Error!!!',err)
      },
    })

    this.listMajor = this.majorsv.getAllMajors();
    this.listMajor.subscribe({
      next(value) {
        // logic code
        console.log('get data ok' + value)
    },
    error(err) {
        console.log('Error!!!',err)
    },
    })
  }


  onSearch() {
    const {key} = this.addForm.value;
    this.listDoctor = this.doctorsv.getAllDoctorByMajor(null,'true',null,key);
    this.listDoctor.subscribe({
      next(value) {
        console.log('get data ok' + value)
      },
      error(err) {
          console.log('Error!!!',err)
      },
    })
  }

  onSelect() {
    console.log(this.khoa)
    this.listDoctor = this.doctorsv.getAllDoctorByMajor(null,'true',this.khoa,null);
    this.listDoctor.subscribe({
      next(value) {
        // logic code
    },
    error(err) {
        console.log('Error!!!',err)
    },
    })
  }

  xem(id : number) {
    this.route.navigate([`public/bac-si-chi-tiet/${id}`])
  }

}
