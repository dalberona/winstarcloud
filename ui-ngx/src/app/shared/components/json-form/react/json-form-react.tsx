/*
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
import * as React from 'react';
import { createTheme, ThemeProvider } from '@material-ui/core/styles';
import winstarcloudTheme from './styles/winstarcloudTheme';
import WinstarcloudSchemaForm from './json-form-schema-form';
import { JsonFormProps } from './json-form.models';

const tbTheme = createTheme(winstarcloudTheme);

class ReactSchemaForm extends React.Component<JsonFormProps, {}> {

  static defaultProps: JsonFormProps;

  constructor(props) {
    super(props);
  }

  render() {
    if (this.props.form.length > 0) {
      return <ThemeProvider theme={tbTheme}><WinstarcloudSchemaForm {...this.props} /></ThemeProvider>;
    } else {
      return <div></div>;
    }
  }
}

ReactSchemaForm.defaultProps = {
  isFullscreen: false,
  schema: {},
  form: ['*'],
  groupInfoes: [],
  option: {
    formDefaults: {
      startEmpty: true
    }
  }
};

export default ReactSchemaForm;
