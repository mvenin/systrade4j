import {Component} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {StrategyTask} from "./app.module";
import {Observable} from "rxjs/index";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'systrade4j';

  task: StrategyTask = new StrategyTask();
  strategyResults: string[];

  constructor(private httpClient: HttpClient) {
    this.task.budget= 10000;
    this.task.yearStart= 2010;
    this.task.yearEnd= 2020;
  }

  ref_stocks: any = ["IVV","IPN"];
  ref_bonds: any = ["AGG"];

  runStrategy() {
    let httpOptions = { headers: new HttpHeaders({'Content-Type': 'application/json'}) };
    this.httpClient.post('api/strategy-task', this.task, httpOptions)
      .subscribe((data: any)=>{ this.strategyResults = data.results.split(/\r?\n/);})
     ;
  }
}
