/**
 * Copyright © 2016-2024 The Winstarcloud Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.winstarcloud.server.msa.ui.tabs;

import org.openqa.selenium.WebDriver;

public class AssignDeviceTabHelper extends AssignDeviceTabElements {
    public AssignDeviceTabHelper(WebDriver driver) {
        super(driver);
    }

    public void assignOnCustomer(String customerTitle) {
        assignOnCustomerField().click();
        customerFromDropDown(customerTitle).click();
        assignBtn().click();
    }
}
