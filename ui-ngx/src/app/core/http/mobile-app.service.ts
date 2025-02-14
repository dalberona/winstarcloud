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

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { defaultHttpOptionsFromConfig, RequestConfig } from '@core/http/http-utils';
import { Observable } from 'rxjs';
import { MobileAppSettings } from '@shared/models/mobile-app.models';

@Injectable({
  providedIn: 'root'
})
export class MobileAppService {

  constructor(
    private http: HttpClient
  ) {
  }

  public getMobileAppSettings(config?: RequestConfig): Observable<MobileAppSettings> {
    return this.http.get<MobileAppSettings>(`/api/mobile/app/settings`, defaultHttpOptionsFromConfig(config));
  }

  public saveMobileAppSettings(mobileAppSettings: MobileAppSettings, config?: RequestConfig): Observable<MobileAppSettings> {
    return this.http.post<MobileAppSettings>(`/api/mobile/app/settings`, mobileAppSettings, defaultHttpOptionsFromConfig(config));
  }

  public getMobileAppDeepLink(config?: RequestConfig): Observable<string> {
    return this.http.get<string>(`/api/mobile/deepLink`, defaultHttpOptionsFromConfig(config));
  }

}
