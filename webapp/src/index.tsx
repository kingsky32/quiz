import './styles/reset.css';
import './styles/index.css';
import 'react-toastify/dist/ReactToastify.css';

import React from 'react';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import duration from 'dayjs/plugin/duration';
import ReactDOM from 'react-dom/client';
import { MemoryRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import Api from './apis/api';
import App from './app/App';

dayjs.extend(relativeTime);
dayjs.extend(duration);

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement,
);
root.render(
  <MemoryRouter>
    <Api.Provider>
      <App />
      <ToastContainer position="bottom-left" />
    </Api.Provider>
  </MemoryRouter>,
);
