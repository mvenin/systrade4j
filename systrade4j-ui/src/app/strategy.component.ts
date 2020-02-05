import {Component} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {StrategyTask} from "./strategy.model";

@Component({
  selector: 'strategy',
  templateUrl: './strategy.component.html',
  styleUrls: ['./app.component.css']
})
export class StrategyComponent {
  task: StrategyTask = new StrategyTask();
  strategyResults: string[];

  constructor(private httpClient: HttpClient) {
    this.task.budget= 10000;
    this.task.yearStart= 2010;
    this.task.yearEnd= 2020;
    this.task.stock= "IVV";
    this.task.bond= "AGG";
    this.task.buyStockRule= "SMA50";
    this.task.sellStockRule= "50";
  }

  ref_stocks: any = ["IVV","IPN", "TSLA"];
  ref_bonds: any = ["AGG"];

  runStrategy() {
    let httpOptions = { headers: new HttpHeaders({'Content-Type': 'application/json'}) };
    this.httpClient.post('api/strategy-task', this.task, httpOptions)
      .subscribe((data: any)=>{ this.strategyResults = data.results.split(/\r?\n/);})
     ;
  }

}
