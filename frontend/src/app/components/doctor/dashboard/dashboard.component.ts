import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  data: any;

  options: any;

  ngOnInit() {
    this.data = {
      labels: ['A : Lịch đã khám', 'B: lịch bị hủy'],
      datasets: [
        {
          data: [540, 325],
          backgroundColor: [
            'yellow',
            'green'
          ],
          hoverBackgroundColor: [
            'black',
            'black',
          ],
        },
      ],
    };

    this.options = {
      plugins: {
        legend: {
          labels: {
            usePointStyle: true,
            color: 'black',
          },
        },
      },
    };
  }
}
