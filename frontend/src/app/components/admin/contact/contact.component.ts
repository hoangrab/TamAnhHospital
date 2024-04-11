import { Component } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Contact } from 'src/app/models/contact';
import { ContactService } from 'src/app/services/contact.service';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.scss']
})
export class ContactComponent {
  lcontact : any;
  dtOption: DataTables.Settings = {}
  dtTrigger : Subject<any> = new Subject<any>();

  itemout: Contact={
    id: 0,
    dob: '',
    name: '',
    note: '',
    gmail: '',
    phone: ''
  };
  constructor(private contactSv: ContactService){};

  ngOnInit() {
    this.dtOption = {
      pagingType: 'full_numbers'
    }
    this.contactSv.getAllContact().subscribe(res => {
      this.lcontact = res;
      this.dtTrigger.next(null);
    })
    
  }

  xem(item : Contact) {
    this.itemout = item;
  }
}
