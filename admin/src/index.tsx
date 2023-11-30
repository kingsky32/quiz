import './styles/reset.css';
import './styles/index.css';

import React from 'react';
import ReactDOM from 'react-dom/client';
import { App as AntdApp } from 'antd';
import { MemoryRouter } from 'react-router-dom';
import Api from './apis/api';
import App from './app/App';

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement,
);
root.render(
  <MemoryRouter>
    <AntdApp>
      <Api.Provider>
        <App />
      </Api.Provider>
    </AntdApp>
  </MemoryRouter>,
);
