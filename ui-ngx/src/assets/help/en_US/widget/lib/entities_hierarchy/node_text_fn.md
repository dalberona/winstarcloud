#### Node text function

<div class="divider"></div>
<br/>

*function (nodeCtx): string*

A JavaScript function used to compute text or HTML code for the current node.

**Parameters:**

<ul>
  <li><b>widgetCtx:</b> <code><a href="https://github.com/winstarcloud/winstarcloud/blob/5bb6403407aa4898084832d6698aa9ea6d484889/ui-ngx/src/app/modules/home/models/widget-component.models.ts#L107" target="_blank">WidgetContext</a></code> - A reference to <a href="https://github.com/winstarcloud/winstarcloud/blob/5bb6403407aa4898084832d6698aa9ea6d484889/ui-ngx/src/app/modules/home/models/widget-component.models.ts#L107" target="_blank">WidgetContext</a> that has all necessary API 
     and data used by widget instance.
  </li>
  <li><b>nodeCtx:</b> <code><a href="https://github.com/winstarcloud/winstarcloud/blob/e264f7b8ddff05bda85c4833bf497f47f447496e/ui-ngx/src/app/modules/home/components/widget/lib/entities-hierarchy-widget.models.ts#L35" target="_blank">HierarchyNodeContext</a></code> - An 
            <a href="https://github.com/winstarcloud/winstarcloud/blob/e264f7b8ddff05bda85c4833bf497f47f447496e/ui-ngx/src/app/modules/home/components/widget/lib/entities-hierarchy-widget.models.ts#L35" target="_blank">HierarchyNodeContext</a> object
            containing <code>entity</code> field holding basic entity properties <br> (ex. <code>id</code>, <code>name</code>, <code>label</code>) and <code>data</code> field holding other entity attributes/timeseries declared in widget datasource configuration.
   </li>
</ul>

**Returns:**

Should return string value presenting text or HTML for the current node.

<div class="divider"></div>

##### Examples

* Display entity name and optionally temperature value if it is present in entity attributes/timeseries:

```javascript
var data =  nodeCtx.data;
var entity = nodeCtx.entity;
var text = entity.name;
if (data.hasOwnProperty('temperature') && data['temperature'] !== null) {
  text += " <b>"+ data['temperature'] +" °C</b>";
}
return text;
{:copy-code}
```

<br>
<br>
