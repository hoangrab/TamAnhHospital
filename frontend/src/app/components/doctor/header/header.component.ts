import { Component, Renderer2, inject } from '@angular/core';
import { Router } from '@angular/router';
import { storageUtils } from 'src/app/utils/storage';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {


  constructor(private renderer : Renderer2, private router : Router){} 
    hien(){
      document.body.classList.toggle('toggle-sidebar');
    }
  
  signout() {
    storageUtils.clear()
    this.router.navigateByUrl('/login')
  }
}
