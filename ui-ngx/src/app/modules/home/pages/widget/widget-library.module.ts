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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '@shared/shared.module';
import { WidgetsBundleComponent } from '@modules/home/pages/widget/widgets-bundle.component';
import { WidgetLibraryRoutingModule } from '@modules/home/pages/widget/widget-library-routing.module';
import { HomeComponentsModule } from '@modules/home/components/home-components.module';
import { WidgetEditorComponent } from '@home/pages/widget/widget-editor.component';
import { SelectWidgetTypeDialogComponent } from '@home/pages/widget/select-widget-type-dialog.component';
import { SaveWidgetTypeAsDialogComponent } from './save-widget-type-as-dialog.component';
import { WidgetsBundleTabsComponent } from '@home/pages/widget/widgets-bundle-tabs.component';
import { WidgetTypeComponent } from '@home/pages/widget/widget-type.component';
import { WidgetTypeTabsComponent } from '@home/pages/widget/widget-type-tabs.component';
import { WidgetsBundleWidgetsComponent } from '@home/pages/widget/widgets-bundle-widgets.component';
import { WidgetTypeAutocompleteComponent } from '@home/pages/widget/widget-type-autocomplete.component';

@NgModule({
  declarations: [
    WidgetTypeComponent,
    WidgetsBundleComponent,
    WidgetTypeAutocompleteComponent,
    WidgetsBundleWidgetsComponent,
    WidgetEditorComponent,
    SelectWidgetTypeDialogComponent,
    SaveWidgetTypeAsDialogComponent,
    WidgetTypeTabsComponent,
    WidgetsBundleTabsComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    HomeComponentsModule,
    WidgetLibraryRoutingModule
  ]
})
export class WidgetLibraryModule { }
