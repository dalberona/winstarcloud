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

import { ChangeDetectorRef, Component, ElementRef, Input, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { PageComponent } from '@shared/components/page.component';
import { AppState } from '@core/core.state';
import { Store } from '@ngrx/store';
import { BadgePosition, MobileAppSettings } from '@shared/models/mobile-app.models';
import { MobileAppService } from '@core/http/mobile-app.service';
import { WidgetContext } from '@home/models/widget-component.models';
import { UtilsService } from '@core/services/utils.service';
import { Observable, Subject } from 'rxjs';
import { MINUTE } from '@shared/models/time/time.models';
import { isDefinedAndNotNull, mergeDeep } from '@core/utils';
import { ResizeObserver } from '@juggle/resize-observer';
import { backgroundStyle, ComponentStyle, overlayStyle } from '@shared/models/widget-settings.models';
import { ImagePipe } from '@shared/pipe/image.pipe';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'tb-mobile-app-qrcode-widget',
  templateUrl: './mobile-app-qrcode-widget.component.html',
  styleUrls: ['./mobile-app-qrcode-widget.component.scss']
})
export class MobileAppQrcodeWidgetComponent extends PageComponent implements OnInit, OnDestroy {

  private readonly destroy$ = new Subject<void>();
  private widgetResize$: ResizeObserver;

  private mobileAppSettingsValue: MobileAppSettings;
  private deepLink: string;
  private deepLinkTTL: number;
  private deepLinkTTLTimeoutID: NodeJS.Timeout;

  googlePlayLink: string;
  appStoreLink: string;

  previewMode = false;

  badgePosition = BadgePosition;
  showBadgeContainer = true;

  backgroundStyle$: Observable<ComponentStyle>;
  overlayStyle: ComponentStyle = {};
  padding: string;

  qrCodeSVG = '';

  @Input()
  ctx: WidgetContext;

  @Input()
  widgetTitlePanel: TemplateRef<any>;

  @Input()
  set mobileAppSettings(settings: MobileAppSettings) {
    if (settings) {
      this.mobileAppSettingsValue = settings;
    }
  };

  get mobileAppSettings(): MobileAppSettings {
    return this.mobileAppSettingsValue;
  }

  constructor(protected store: Store<AppState>,
              protected cd: ChangeDetectorRef,
              private mobileAppService: MobileAppService,
              private utilsService: UtilsService,
              private elementRef: ElementRef,
              private imagePipe: ImagePipe,
              private sanitizer: DomSanitizer,) {
    super(store);
  }

  ngOnInit(): void {
    if (!this.mobileAppSettings) {
      this.mobileAppService.getMobileAppSettings().subscribe((settings => {
        this.mobileAppSettings = settings;

        const useDefaultApp = this.mobileAppSettings.useDefaultApp;
        this.appStoreLink = useDefaultApp ? this.mobileAppSettings.defaultAppStoreLink :
          this.mobileAppSettings.iosConfig.storeLink;
        this.googlePlayLink = useDefaultApp ? this.mobileAppSettings.defaultGooglePlayLink :
          this.mobileAppSettings.androidConfig.storeLink;

        if (isDefinedAndNotNull(this.ctx.settings.useSystemSettings) && !this.ctx.settings.useSystemSettings) {
          this.mobileAppSettings = mergeDeep(this.mobileAppSettings, this.ctx.settings);
        }

        this.widgetResize$ = new ResizeObserver(() => {
          const showHideBadgeContainer = this.elementRef.nativeElement.offsetWidth > 250;
          if (showHideBadgeContainer !== this.showBadgeContainer) {
            this.showBadgeContainer = showHideBadgeContainer;
            this.cd.markForCheck();
          }
        });

        this.widgetResize$.observe(this.elementRef.nativeElement);
        this.backgroundStyle$ = backgroundStyle(this.ctx.settings.background, this.imagePipe, this.sanitizer);
        this.overlayStyle = overlayStyle(this.ctx.settings.background.overlay);
        this.padding = this.ctx.settings.background.overlay.enabled ? undefined : this.ctx.settings.padding;
        this.cd.markForCheck();
      }));
    } else {
      this.previewMode = true;
    }
    this.initMobileAppQRCode();
  }

  ngOnDestroy() {
    if (this.widgetResize$) {
      this.widgetResize$.disconnect();
    }
    super.ngOnDestroy();
    this.destroy$.next();
    this.destroy$.complete();
    clearTimeout(this.deepLinkTTLTimeoutID);
  }

  navigateByDeepLink($event) {
    if ($event) {
      $event.stopPropagation();
    }
    if (this.ctx.isMobile) {
      window.open(this.deepLink, '_blank');
    }
  }

  private initMobileAppQRCode() {
    if (this.deepLinkTTLTimeoutID) {
      clearTimeout(this.deepLinkTTLTimeoutID);
      this.deepLinkTTLTimeoutID = null;
    }
    this.mobileAppService.getMobileAppDeepLink().subscribe(link => {
      this.deepLink = link;
      this.deepLinkTTL = Number(this.utilsService.getQueryParam('ttl', link)) * MINUTE;
      this.updateQRCode(link);
      this.deepLinkTTLTimeoutID = setTimeout(() => this.initMobileAppQRCode(), this.deepLinkTTL);
    });
  }

  private updateQRCode(link: string) {
    import('qrcode').then((QRCode) => {
      QRCode.toString(link, (err, string) => {
        this.qrCodeSVG = string;
        this.cd.markForCheck();
      })
    });
  }

}
