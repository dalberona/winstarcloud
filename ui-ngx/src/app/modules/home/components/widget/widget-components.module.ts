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
import { SharedModule } from '@app/shared/shared.module';
import { EntitiesTableWidgetComponent } from '@home/components/widget/lib/entity/entities-table-widget.component';
import { DisplayColumnsPanelComponent } from '@home/components/widget/lib/display-columns-panel.component';
import { AlarmsTableWidgetComponent } from '@home/components/widget/lib/alarm/alarms-table-widget.component';
import { SharedHomeComponentsModule } from '@home/components/shared-home-components.module';
import { TimeseriesTableWidgetComponent } from '@home/components/widget/lib/timeseries-table-widget.component';
import {
  EntitiesHierarchyWidgetComponent
} from '@home/components/widget/lib/entity/entities-hierarchy-widget.component';
import { RpcWidgetsModule } from '@home/components/widget/lib/rpc/rpc-widgets.module';
import {
  DateRangeNavigatorPanelComponent,
  DateRangeNavigatorWidgetComponent
} from '@home/components/widget/lib/date-range-navigator/date-range-navigator.component';
import { MultipleInputWidgetComponent } from '@home/components/widget/lib/multiple-input-widget.component';
import { TripAnimationComponent } from '@home/components/widget/lib/trip-animation/trip-animation.component';
import { PhotoCameraInputWidgetComponent } from '@home/components/widget/lib/photo-camera-input.component';
import { GatewayFormComponent } from '@home/components/widget/lib/gateway/gateway-form.component';
import { NavigationCardsWidgetComponent } from '@home/components/widget/lib/navigation-cards-widget.component';
import { NavigationCardWidgetComponent } from '@home/components/widget/lib/navigation-card-widget.component';
import { EdgesOverviewWidgetComponent } from '@home/components/widget/lib/edges-overview-widget.component';
import { JsonInputWidgetComponent } from '@home/components/widget/lib/json-input-widget.component';
import { QrCodeWidgetComponent } from '@home/components/widget/lib/qrcode-widget.component';
import { MarkdownWidgetComponent } from '@home/components/widget/lib/markdown-widget.component';
import { SelectEntityDialogComponent } from '@home/components/widget/lib/maps/dialogs/select-entity-dialog.component';
import { HomePageWidgetsModule } from '@home/components/widget/lib/home-page/home-page-widgets.module';
import { WIDGET_COMPONENTS_MODULE_TOKEN } from '@home/components/tokens';
import { FlotWidgetComponent } from '@home/components/widget/lib/flot-widget.component';
import { LegendComponent } from '@home/components/widget/lib/legend.component';
import { GatewayConnectorComponent } from '@home/components/widget/lib/gateway/gateway-connectors.component';
import { GatewayLogsComponent } from '@home/components/widget/lib/gateway/gateway-logs.component';
import { GatewayStatisticsComponent } from '@home/components/widget/lib/gateway/gateway-statistics.component';
import { GatewayServiceRPCComponent } from '@home/components/widget/lib/gateway/gateway-service-rpc.component';
import {
  GatewayServiceRPCConnectorComponent
} from '@home/components/widget/lib/gateway/gateway-service-rpc-connector.component';
import {
  GatewayServiceRPCConnectorTemplatesComponent
} from '@home/components/widget/lib/gateway/gateway-service-rpc-connector-templates.component';
import { DeviceGatewayCommandComponent } from '@home/components/widget/lib/gateway/device-gateway-command.component';
import { GatewayConfigurationComponent } from '@home/components/widget/lib/gateway/gateway-configuration.component';
import {
  GatewayRemoteConfigurationDialogComponent
} from '@home/components/widget/lib/gateway/gateway-remote-configuration-dialog';
import { ValueCardWidgetComponent } from '@home/components/widget/lib/cards/value-card-widget.component';
import {
  AggregatedValueCardWidgetComponent
} from '@home/components/widget/lib/cards/aggregated-value-card-widget.component';
import { CountWidgetComponent } from '@home/components/widget/lib/count/count-widget.component';
import { BatteryLevelWidgetComponent } from '@home/components/widget/lib/indicator/battery-level-widget.component';
import {
  WindSpeedDirectionWidgetComponent
} from '@home/components/widget/lib/weather/wind-speed-direction-widget.component';
import { SignalStrengthWidgetComponent } from '@home/components/widget/lib/indicator/signal-strength-widget.component';
import { ValueChartCardWidgetComponent } from '@home/components/widget/lib/cards/value-chart-card-widget.component';
import { ProgressBarWidgetComponent } from '@home/components/widget/lib/cards/progress-bar-widget.component';
import { LiquidLevelWidgetComponent } from '@home/components/widget/lib/indicator/liquid-level-widget.component';
import { DoughnutWidgetComponent } from '@home/components/widget/lib/chart/doughnut-widget.component';
import { RangeChartWidgetComponent } from '@home/components/widget/lib/chart/range-chart-widget.component';
import {
  BarChartWithLabelsWidgetComponent
} from '@home/components/widget/lib/chart/bar-chart-with-labels-widget.component';
import {
  GatewayServiceRPCConnectorTemplateDialogComponent
} from '@home/components/widget/lib/gateway/gateway-service-rpc-connector-template-dialog';
import { SingleSwitchWidgetComponent } from '@home/components/widget/lib/rpc/single-switch-widget.component';
import { ActionButtonWidgetComponent } from '@home/components/widget/lib/button/action-button-widget.component';
import { CommandButtonWidgetComponent } from '@home/components/widget/lib/button/command-button-widget.component';
import { PowerButtonWidgetComponent } from '@home/components/widget/lib/rpc/power-button-widget.component';
import { SliderWidgetComponent } from '@home/components/widget/lib/rpc/slider-widget.component';
import { ToggleButtonWidgetComponent } from '@home/components/widget/lib/button/toggle-button-widget.component';
import { TimeSeriesChartWidgetComponent } from '@home/components/widget/lib/chart/time-series-chart-widget.component';
import { AddConnectorDialogComponent } from '@home/components/widget/lib/gateway/dialog/add-connector-dialog.component';
import { MappingDialogComponent } from '@home/components/widget/lib/gateway/dialog/mapping-dialog.component';
import {
  EllipsisChipListDirective
} from '@home/components/widget/lib/gateway/connectors-configuration/ellipsis-chip-list.directive';
import { StatusWidgetComponent } from '@home/components/widget/lib/indicator/status-widget.component';
import { LatestChartComponent } from '@home/components/widget/lib/chart/latest-chart.component';
import { PieChartWidgetComponent } from '@home/components/widget/lib/chart/pie-chart-widget.component';
import { BarChartWidgetComponent } from '@home/components/widget/lib/chart/bar-chart-widget.component';
import { PolarAreaWidgetComponent } from '@home/components/widget/lib/chart/polar-area-widget.component';
import { RadarChartWidgetComponent } from '@home/components/widget/lib/chart/radar-chart-widget.component';
import { MobileAppQrcodeWidgetComponent } from '@home/components/widget/lib/mobile-app-qrcode-widget.component';
import { KeyValueIsNotEmptyPipe } from '@shared/pipe/key-value-not-empty.pipe';
import { LabelCardWidgetComponent } from '@home/components/widget/lib/cards/label-card-widget.component';
import { LabelValueCardWidgetComponent } from '@home/components/widget/lib/cards/label-value-card-widget.component';
import {
  RestConnectorSecurityComponent
} from '@home/components/widget/lib/gateway/connectors-configuration/rest-connector-secuirity/rest-connector-security.component';
import {
  UnreadNotificationWidgetComponent
} from '@home/components/widget/lib/cards/unread-notification-widget.component';
import {
  NotificationTypeFilterPanelComponent
} from '@home/components/widget/lib/cards/notification-type-filter-panel.component';
import { GatewayHelpLinkPipe } from '@home/pipes/public-api';
import {
  BrokerConfigControlComponent,
  DeviceInfoTableComponent,
  MappingDataKeysPanelComponent,
  MappingTableComponent,
  MqttBasicConfigComponent,
  OpcUaBasicConfigComponent,
  ServerConfigComponent,
  TypeValuePanelComponent,
  WorkersConfigControlComponent,
} from '@home/components/widget/lib/gateway/connectors-configuration/public-api';

@NgModule({
  declarations: [
    DisplayColumnsPanelComponent,
    EntitiesTableWidgetComponent,
    AlarmsTableWidgetComponent,
    TimeseriesTableWidgetComponent,
    EntitiesHierarchyWidgetComponent,
    EdgesOverviewWidgetComponent,
    DateRangeNavigatorWidgetComponent,
    DateRangeNavigatorPanelComponent,
    JsonInputWidgetComponent,
    MultipleInputWidgetComponent,
    TripAnimationComponent,
    PhotoCameraInputWidgetComponent,
    GatewayFormComponent,
    NavigationCardsWidgetComponent,
    NavigationCardWidgetComponent,
    QrCodeWidgetComponent,
    MobileAppQrcodeWidgetComponent,
    MarkdownWidgetComponent,
    SelectEntityDialogComponent,
    LegendComponent,
    FlotWidgetComponent,
    GatewayConnectorComponent,
    AddConnectorDialogComponent,
    MappingDialogComponent,
    DeviceInfoTableComponent,
    MappingDataKeysPanelComponent,
    TypeValuePanelComponent,
    GatewayLogsComponent,
    GatewayStatisticsComponent,
    GatewayServiceRPCComponent,
    GatewayServiceRPCConnectorComponent,
    GatewayServiceRPCConnectorTemplatesComponent,
    DeviceGatewayCommandComponent,
    GatewayConfigurationComponent,
    GatewayRemoteConfigurationDialogComponent,
    GatewayServiceRPCConnectorTemplateDialogComponent,
    EllipsisChipListDirective,
    ValueCardWidgetComponent,
    AggregatedValueCardWidgetComponent,
    CountWidgetComponent,
    BatteryLevelWidgetComponent,
    WindSpeedDirectionWidgetComponent,
    SignalStrengthWidgetComponent,
    ValueChartCardWidgetComponent,
    ProgressBarWidgetComponent,
    LiquidLevelWidgetComponent,
    DoughnutWidgetComponent,
    RangeChartWidgetComponent,
    BarChartWithLabelsWidgetComponent,
    SingleSwitchWidgetComponent,
    ActionButtonWidgetComponent,
    CommandButtonWidgetComponent,
    PowerButtonWidgetComponent,
    SliderWidgetComponent,
    ToggleButtonWidgetComponent,
    TimeSeriesChartWidgetComponent,
    StatusWidgetComponent,
    LatestChartComponent,
    PieChartWidgetComponent,
    BarChartWidgetComponent,
    PolarAreaWidgetComponent,
    RadarChartWidgetComponent,
    LabelCardWidgetComponent,
    LabelValueCardWidgetComponent,
    UnreadNotificationWidgetComponent,
    NotificationTypeFilterPanelComponent],
  imports: [
    CommonModule,
    SharedModule,
    RpcWidgetsModule,
    HomePageWidgetsModule,
    SharedHomeComponentsModule,
    RestConnectorSecurityComponent,
    GatewayHelpLinkPipe,
    BrokerConfigControlComponent,
    WorkersConfigControlComponent,
    ServerConfigComponent,
    MqttBasicConfigComponent,
    MappingTableComponent,
    OpcUaBasicConfigComponent,
    KeyValueIsNotEmptyPipe
  ],
  exports: [
    EntitiesTableWidgetComponent,
    AlarmsTableWidgetComponent,
    TimeseriesTableWidgetComponent,
    EntitiesHierarchyWidgetComponent,
    EdgesOverviewWidgetComponent,
    RpcWidgetsModule,
    HomePageWidgetsModule,
    DateRangeNavigatorWidgetComponent,
    JsonInputWidgetComponent,
    MultipleInputWidgetComponent,
    TripAnimationComponent,
    PhotoCameraInputWidgetComponent,
    GatewayFormComponent,
    NavigationCardsWidgetComponent,
    NavigationCardWidgetComponent,
    QrCodeWidgetComponent,
    MobileAppQrcodeWidgetComponent,
    MarkdownWidgetComponent,
    LegendComponent,
    FlotWidgetComponent,
    GatewayConnectorComponent,
    AddConnectorDialogComponent,
    MappingDialogComponent,
    DeviceInfoTableComponent,
    MappingDataKeysPanelComponent,
    TypeValuePanelComponent,
    GatewayLogsComponent,
    GatewayServiceRPCConnectorComponent,
    GatewayServiceRPCConnectorTemplatesComponent,
    EllipsisChipListDirective,
    GatewayStatisticsComponent,
    GatewayServiceRPCComponent,
    DeviceGatewayCommandComponent,
    GatewayConfigurationComponent,
    GatewayRemoteConfigurationDialogComponent,
    GatewayServiceRPCConnectorTemplateDialogComponent,
    ValueCardWidgetComponent,
    AggregatedValueCardWidgetComponent,
    CountWidgetComponent,
    BatteryLevelWidgetComponent,
    WindSpeedDirectionWidgetComponent,
    SignalStrengthWidgetComponent,
    ValueChartCardWidgetComponent,
    ProgressBarWidgetComponent,
    LiquidLevelWidgetComponent,
    DoughnutWidgetComponent,
    RangeChartWidgetComponent,
    BarChartWithLabelsWidgetComponent,
    SingleSwitchWidgetComponent,
    ActionButtonWidgetComponent,
    CommandButtonWidgetComponent,
    PowerButtonWidgetComponent,
    SliderWidgetComponent,
    ToggleButtonWidgetComponent,
    TimeSeriesChartWidgetComponent,
    StatusWidgetComponent,
    PieChartWidgetComponent,
    BarChartWidgetComponent,
    PolarAreaWidgetComponent,
    RadarChartWidgetComponent,
    LabelCardWidgetComponent,
    LabelValueCardWidgetComponent,
    UnreadNotificationWidgetComponent,
    NotificationTypeFilterPanelComponent
  ],
  providers: [
    {provide: WIDGET_COMPONENTS_MODULE_TOKEN, useValue: WidgetComponentsModule}
  ]
})
export class WidgetComponentsModule {
}
