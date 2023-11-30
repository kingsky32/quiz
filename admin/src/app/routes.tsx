import React from 'react';
import QuizCategoryManagementPage from './quiz/category_management/page';
import QuizManagementPage from './quiz/management/page';
import QuizManagementCreatePage from './quiz/management/create/page';
import QuizManagementDetailPage from './quiz/management/[id]/page';

export interface Route {
  path: string;
  name: string;
  menu: boolean;
  auth?: boolean;
  element?: React.ReactNode;
  children?: Route[];
}

const routes: Route[] = [
  {
    path: '/admin/quiz',
    name: '퀴즈',
    menu: true,
    children: [
      {
        path: '/admin/quiz/category_management',
        name: '퀴즈 분류 관리',
        menu: true,
        element: <QuizCategoryManagementPage />,
      },
      {
        path: '/admin/quiz/management',
        name: '퀴즈 관리',
        menu: true,
        element: <QuizManagementPage />,
        children: [
          {
            path: '/admin/quiz/management/create',
            name: '퀴즈 등록',
            menu: false,
            element: <QuizManagementCreatePage />,
          },
          {
            path: '/admin/quiz/management/:id',
            name: '퀴즈 수정',
            menu: false,
            element: <QuizManagementDetailPage />,
          },
        ],
      },
    ],
  },
];

export default routes;
