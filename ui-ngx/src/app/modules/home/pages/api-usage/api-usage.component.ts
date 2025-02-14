///
/// Copyright © 2016-2024 The Winstarcloud Authors
///
/// Licensed under the Apache License, Version 2.0 (the "License");
/// you may not use this file except in compliance with the License.
/// You may obtain a copy of the License at
///
///     http://www.apache.org/licenses/LICENSE-2.0
///
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS,
/// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/// See the License for the specific language governing permissions and
/// limitations under the License.
///

import { Component } from '@angular/core';
import { Store } from '@ngrx/store';
import { AppState } from '@core/core.state';
import { PageComponent } from '@shared/components/page.component';
import { Dashboard } from '@shared/models/dashboard.models';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'tb-api-usage',
  templateUrl: './api-usage.component.html',
  styleUrls: ['./api-usage.component.scss']
})
export class ApiUsageComponent extends PageComponent {

  apiUsageDashboard: Dashboard = this.route.snapshot.data.apiUsageDashboard;

  constructor(protected store: Store<AppState>,
              private route: ActivatedRoute) {
    super(store);
  }
}
