import { formatDate } from '@angular/common';
import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DataTableDirective } from 'angular-datatables';
import DataTables from 'datatables.net';
import { data } from 'jquery';
import { Observable, Subject } from 'rxjs';
import { Booking } from 'src/app/models/booking';
import { Hours } from 'src/app/models/hour';
import { Schedule } from 'src/app/models/schedule';
import { BookingService } from 'src/app/services/booking.service';
import { ScheduleService } from 'src/app/services/schedule.service';
import { storageUtils } from 'src/app/utils/storage';

@Component({
  selector: 'app-schedule',
  templateUrl: './schedule.component.html',
  styleUrls: ['./schedule.component.scss']
})
export class ScheduleComponent {
  lschedule : any;
  lbooking : any;
  string1 = ''
  addForm!:FormGroup
  dtTrigger:Subject<any>=new Subject<any>();
  dtOption: DataTables.Settings =  {}
  @ViewChild(DataTableDirective, {static: false})
  dtElement!: DataTableDirective;
  hours = Hours;
  constructor(private scheSv: ScheduleService, private bookingsv : BookingService,
    private formbuilder : FormBuilder, private changref: ChangeDetectorRef){};

  ngOnInit() {
    this.dtOption = {
      pagingType: 'full_numbers',
      renderer: 'true',
      retrieve:true,
      info:false
    }
    this.addForm = this.formbuilder.group({
      start : ['',Validators.required],
      end : ['',Validators.required]
    })

    this.bookingsv.getAllBooking('ACCEPTING',storageUtils.get('userId') || null,null,null).subscribe(res => {
      this.lbooking = res;
      this.changref.detectChanges()
      this.dtTrigger.next(null)
    })
  }

  onSearch() {
    const {start,end} = this.addForm.value;
    let startformat = ''
    let endformat = ''
    if(start != '') {
      let s =  start.split('-')
      startformat = s[2] + '/' + s[1] + '/' + s[0];
    }
    if(end != '') {
      let s =  end.split('-')
      endformat = s[2] + '/' + s[1] + '/' + s[0];
    }
    console.log(startformat + "---" + endformat)
    this.bookingsv.getAllBooking('ACCEPTING',storageUtils.get('userId') || null,startformat,endformat).subscribe(res =>{
      this.lbooking = res;
    })
  }

  loaddata() {
    this.bookingsv.getAllBooking('ACCEPTING',storageUtils.get('userId') || null,null,null).subscribe(res => {
      this.lbooking = res;
      this.dtElement.dtInstance.then((dtInstance: DataTables.Api) => {
        // Destroy the table first
        dtInstance.destroy();
        // Call the dtTrigger to rerender again
        this.dtTrigger.next(null);
      });
    })
  }

  onReset() {
    this.loaddata()
    this.addForm.patchValue({
      start : '',
      end : ''
    })
  }

  onSearchToday() {
    this.addForm.patchValue({
      start : '',
      end : ''
    })
    let date = new Date();
    let st = formatDate(date, 'dd/MM/yyyy', 'en-US')
    this.bookingsv.getAllBooking('ACCEPTING',storageUtils.get('userId') || null,st,st).subscribe(res =>{
      this.lbooking = res;
      this.dtTrigger.next(null)
    })
  }
  ngOnDestroy() {
      this.dtTrigger.unsubscribe()
  }

  xacnhan(id: number, status: string) {
    this.bookingsv.updateBooking(id,status).subscribe({
      next : (value) => {
          this.lbooking = value;
          this.loaddata()
          alert('Đã xác nhận thành công')
      },
      error(err) {
          alert('Đã xảy ra lỗi: ' + err)
      },
    })
  }
}
